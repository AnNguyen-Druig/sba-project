package com.sba.project.modules.utilities.service.impl;

import com.sba.project.common.exceptions.AppException;
import com.sba.project.common.exceptions.ErrorCode;
import com.sba.project.modules.utilities.dto.request.CreateRoomServiceChargeRequest;
import com.sba.project.modules.utilities.dto.request.FinalizeChargeRequest;
import com.sba.project.modules.utilities.dto.response.ChargeDetailResponse;
import com.sba.project.modules.utilities.dto.response.RoomServiceChargeResponse;
import com.sba.project.modules.utilities.entity.RoomService;
import com.sba.project.modules.utilities.entity.RoomServiceCharge;
import com.sba.project.modules.utilities.entity.ServiceCatalog;
import com.sba.project.modules.utilities.enums.BillingMethod;
import com.sba.project.modules.utilities.enums.ChargeSegmentReason;
import com.sba.project.modules.utilities.enums.RoomServiceChargeStatus;
import com.sba.project.modules.utilities.mapper.RoomServiceChargeMapper;
import com.sba.project.modules.utilities.repository.RoomServiceChargeRepository;
import com.sba.project.modules.utilities.repository.RoomServiceRepository;
import com.sba.project.modules.utilities.repository.ServiceCatalogRepository;
import com.sba.project.modules.utilities.service.MeterChargeService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

/**
 * Implementation of {@link MeterChargeService} covering BE-04.2.
 * Handles meter readings, segment reasons (meter change, reset) [P1-74],
 * usage calculation per billing period [P1-75], billing type calculations [P1-76],
 * preserving finalized charge integrity [P1-77], and breakdown reconciliation [P1-78].
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MeterChargeServiceImpl implements MeterChargeService {

    private final RoomServiceChargeRepository roomServiceChargeRepository;
    private final RoomServiceRepository roomServiceRepository;
    private final ServiceCatalogRepository serviceCatalogRepository;
    private final RoomServiceChargeMapper roomServiceChargeMapper;

    /**
     * [P1-75][P1-76] Calculates total charge based on billing method, quantity, and unit price.
     * In Vietnam Dong (VND), charges are rounded to integer currency (scale 0, HALF_UP).
     */
    @Override
    public BigDecimal calculateCharge(BillingMethod method, BigDecimal quantity, BigDecimal unitPrice) {
        if (quantity == null || unitPrice == null) {
            return BigDecimal.ZERO;
        }
        return quantity.multiply(unitPrice).setScale(0, RoundingMode.HALF_UP);
    }

    /**
     * [P1-74][P1-75] Enters meter readings per segment or fixed charges.
     * Supports meter change and meter reset scenarios.
     */
    @Override
    @Transactional
    public RoomServiceChargeResponse createCharge(UUID createdBy, CreateRoomServiceChargeRequest request) {
        log.info("Creating room service charge: roomServiceId={}, billingMonth={}, segment={}",
                request.getRoomServiceId(), request.getBillingMonth(), request.getSegmentNumber());

        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Start date cannot be after end date");
        }

        RoomService roomService = roomServiceRepository.findById(request.getRoomServiceId())
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_SERVICE_NOT_FOUND));

        ServiceCatalog catalog = serviceCatalogRepository.findById(roomService.getServiceId())
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_NOT_FOUND));

        BigDecimal quantity;
        if (catalog.getBillingMethod() == BillingMethod.METERED) {
            quantity = calculateMeteredQuantity(request);
        } else {
            quantity = (request.getQuantity() != null && request.getQuantity().compareTo(BigDecimal.ZERO) > 0)
                    ? request.getQuantity()
                    : BigDecimal.ONE;
        }

        BigDecimal totalAmount = calculateCharge(catalog.getBillingMethod(), quantity, request.getUnitPrice());

        // Build calculation snapshot for audit and reconciliation [P1-78]
        ObjectNode snapshot = JsonNodeFactory.instance.objectNode();
        snapshot.put("serviceName", catalog.getName());
        snapshot.put("billingMethod", catalog.getBillingMethod().name());
        snapshot.put("unitPrice", request.getUnitPrice().toPlainString());
        snapshot.put("quantity", quantity.toPlainString());
        snapshot.put("totalAmount", totalAmount.toPlainString());
        snapshot.put("segmentNumber", request.getSegmentNumber());
        snapshot.put("segmentReason", request.getSegmentReason().name());
        if (request.getStartReading() != null) {
            snapshot.put("startReading", request.getStartReading().toPlainString());
        }
        if (request.getEndReading() != null) {
            snapshot.put("endReading", request.getEndReading().toPlainString());
        }

        RoomServiceCharge charge = RoomServiceCharge.builder()
                .roomServiceId(request.getRoomServiceId())
                .roomId(roomService.getRoomId())
                .billingMonth(request.getBillingMonth())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .segmentNumber(request.getSegmentNumber())
                .meterReference(request.getMeterReference())
                .segmentReason(request.getSegmentReason())
                .startReading(request.getStartReading())
                .endReading(request.getEndReading())
                .quantity(quantity)
                .unitPrice(request.getUnitPrice())
                .totalAmount(totalAmount)
                .calculationSnapshot(snapshot)
                .evidenceRefs(JsonNodeFactory.instance.arrayNode())
                .status(RoomServiceChargeStatus.DRAFT)
                .build();

        RoomServiceCharge saved = roomServiceChargeRepository.save(charge);
        log.info("Saved room service charge id={} with totalAmount={}", saved.getId(), saved.getTotalAmount());
        return roomServiceChargeMapper.toResponse(saved, catalog.getName());
    }

    /**
     * [P1-75][P1-77] Finalizes a batch of charges so they cannot be altered when prices change later.
     */
    @Override
    @Transactional
    public List<RoomServiceChargeResponse> finalizeCharges(UUID finalizedBy, FinalizeChargeRequest request) {
        log.info("Finalizing charges count={}, finalizedBy={}", request.getChargeIds().size(), finalizedBy);

        List<RoomServiceCharge> finalizedList = new ArrayList<>();
        Instant now = Instant.now();

        for (UUID chargeId : request.getChargeIds()) {
            RoomServiceCharge charge = roomServiceChargeRepository.findById(chargeId)
                    .orElseThrow(() -> new AppException(ErrorCode.SERVICE_CHARGE_NOT_FOUND));

            if (charge.getStatus() == RoomServiceChargeStatus.CANCELLED) {
                throw new AppException(ErrorCode.REQUEST_FAILED, "Cannot finalize cancelled charge: " + chargeId);
            }

            if (charge.getStatus() == RoomServiceChargeStatus.FINALIZED) {
                finalizedList.add(charge);
                continue;
            }

            charge.setStatus(RoomServiceChargeStatus.FINALIZED);
            charge.setFinalizedBy(finalizedBy);
            charge.setFinalizedAt(now);

            finalizedList.add(roomServiceChargeRepository.save(charge));
        }

        Map<UUID, String> serviceNameMap = resolveServiceNamesForCharges(finalizedList);

        return finalizedList.stream()
                .map(ch -> roomServiceChargeMapper.toResponse(ch, serviceNameMap.getOrDefault(ch.getId(), "Unknown Service")))
                .toList();
    }

    /**
     * Retrieves charge by ID.
     */
    @Override
    public RoomServiceChargeResponse getCharge(UUID chargeId) {
        RoomServiceCharge charge = roomServiceChargeRepository.findById(chargeId)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_CHARGE_NOT_FOUND));

        String serviceName = resolveServiceNameForCharge(charge);
        return roomServiceChargeMapper.toResponse(charge, serviceName);
    }

    /**
     * Lists charges for a room within a billing month.
     */
    @Override
    public List<RoomServiceChargeResponse> listCharges(UUID roomId, LocalDate billingMonth, RoomServiceChargeStatus status) {
        List<RoomServiceCharge> list = (status != null)
                ? roomServiceChargeRepository.findByRoomIdAndBillingMonthAndStatus(roomId, billingMonth, status)
                : roomServiceChargeRepository.findByRoomIdAndBillingMonth(roomId, billingMonth);

        if (list.isEmpty()) {
            return List.of();
        }

        Map<UUID, String> serviceNameMap = resolveServiceNamesForCharges(list);

        return list.stream()
                .map(ch -> roomServiceChargeMapper.toResponse(ch, serviceNameMap.getOrDefault(ch.getId(), "Unknown Service")))
                .toList();
    }

    /**
     * [P1-78] Retrieves charge breakdown for reconciliation.
     */
    @Override
    public ChargeDetailResponse getChargeDetail(UUID chargeId) {
        RoomServiceCharge charge = roomServiceChargeRepository.findById(chargeId)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_CHARGE_NOT_FOUND));

        RoomService roomService = roomServiceRepository.findById(charge.getRoomServiceId())
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_SERVICE_NOT_FOUND));

        ServiceCatalog catalog = serviceCatalogRepository.findById(roomService.getServiceId())
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_NOT_FOUND));

        return roomServiceChargeMapper.toChargeDetailResponse(charge, catalog.getName(), catalog.getBillingMethod());
    }

    private BigDecimal calculateMeteredQuantity(CreateRoomServiceChargeRequest request) {
        if (request.getStartReading() == null || request.getEndReading() == null) {
            throw new AppException(ErrorCode.INVALID_METER_READING,
                    "Start and end readings are required for metered services");
        }

        if (request.getEndReading().compareTo(request.getStartReading()) < 0) {
            if (request.getSegmentReason() != ChargeSegmentReason.METER_RESET
                    && request.getSegmentReason() != ChargeSegmentReason.METER_CHANGE) {
                throw new AppException(ErrorCode.INVALID_METER_READING,
                        "End reading cannot be less than start reading for regular segment");
            }
            // For meter reset/change with rollover: use provided quantity if available, or endReading as consumed
            if (request.getQuantity() != null && request.getQuantity().compareTo(BigDecimal.ZERO) >= 0) {
                return request.getQuantity();
            }
            return request.getEndReading();
        }

        return request.getEndReading().subtract(request.getStartReading());
    }

    private String resolveServiceNameForCharge(RoomServiceCharge charge) {
        return roomServiceRepository.findById(charge.getRoomServiceId())
                .flatMap(rs -> serviceCatalogRepository.findById(rs.getServiceId()))
                .map(ServiceCatalog::getName)
                .orElse("Unknown Service");
    }

    private Map<UUID, String> resolveServiceNamesForCharges(List<RoomServiceCharge> charges) {
        if (charges == null || charges.isEmpty()) {
            return Collections.emptyMap();
        }

        List<UUID> roomServiceIds = charges.stream()
                .filter(Objects::nonNull)
                .map(RoomServiceCharge::getRoomServiceId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<UUID, RoomService> rsMap = roomServiceIds.isEmpty() ? Collections.emptyMap() :
                Optional.ofNullable(roomServiceRepository.findAllById(roomServiceIds))
                        .orElse(Collections.emptyList())
                        .stream()
                        .filter(Objects::nonNull)
                        .filter(rs -> rs.getId() != null)
                        .collect(Collectors.toMap(RoomService::getId, Function.identity(), (a, b) -> a));

        List<UUID> catIds = rsMap.values().stream()
                .map(RoomService::getServiceId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<UUID, ServiceCatalog> catMap = catIds.isEmpty() ? Collections.emptyMap() :
                Optional.ofNullable(serviceCatalogRepository.findAllById(catIds))
                        .orElse(Collections.emptyList())
                        .stream()
                        .filter(Objects::nonNull)
                        .filter(c -> c.getId() != null)
                        .collect(Collectors.toMap(ServiceCatalog::getId, Function.identity(), (a, b) -> a));

        return charges.stream()
                .filter(Objects::nonNull)
                .filter(ch -> ch.getId() != null)
                .collect(Collectors.toMap(
                        RoomServiceCharge::getId,
                        ch -> {
                            if (ch.getRoomServiceId() != null) {
                                RoomService rs = rsMap.get(ch.getRoomServiceId());
                                if (rs != null && rs.getServiceId() != null) {
                                    ServiceCatalog cat = catMap.get(rs.getServiceId());
                                    if (cat != null && cat.getName() != null) {
                                        return cat.getName();
                                    }
                                }
                            }
                            return "Unknown Service";
                        },
                        (a, b) -> a));
    }
}
