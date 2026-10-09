package com.sba.project.service.impl;

import com.sba.project.dto.request.*;
import com.sba.project.dto.response.*;
import com.sba.project.entity.*;
import com.sba.project.enums.BillingMethod;
import com.sba.project.exception.*;
import com.sba.project.repository.*;
import com.sba.project.service.MeterAndChargeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MeterAndChargeServiceImpl implements MeterAndChargeService {

    private final RoomServiceRepository roomServiceRepository;
    private final RoomServiceChargeRepository chargeRepository;

    @Override
    public RoomServiceChargeResponse createMeterReading(CreateMeterReadingRequest request, UUID createdBy) {
        RoomService roomService = roomServiceRepository.findById(request.getRoomServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dịch vụ phòng: " + request.getRoomServiceId()));

        // [P1-74] Kiểm tra chỉ số hợp lệ
        if (roomService.getService().getBillingMethod() != BillingMethod.BY_METER) {
            throw new BusinessException("Dịch vụ này không tính theo chỉ số");
        }

        if (request.getCurrentReading().compareTo(request.getPreviousReading()) < 0) {
            throw new BusinessException("Chỉ số cuối kỳ phải lớn hơn hoặc bằng chỉ số đầu kỳ");
        }

        if (request.getBillingPeriodEnd().isBefore(request.getBillingPeriodStart())) {
            throw new BusinessException("Ngày kết thúc kỳ phải sau ngày bắt đầu");
        }

        // [P1-75] Kiểm tra trùng kỳ
        if (chargeRepository.existsByRoomServiceIdAndBillingPeriodStartAndBillingPeriodEnd(
                request.getRoomServiceId(), request.getBillingPeriodStart(), request.getBillingPeriodEnd())) {
            throw new DuplicateResourceException("Đã có phí dịch vụ cho kỳ này");
        }

        BigDecimal quantity = request.getCurrentReading().subtract(request.getPreviousReading());
        BigDecimal unitPrice = roomService.getUnitPrice();
        BigDecimal totalAmount = quantity.multiply(unitPrice);

        RoomServiceCharge charge = RoomServiceCharge.builder()
                .roomService(roomService)
                .roomId(request.getRoomId())
                .billingPeriodStart(request.getBillingPeriodStart())
                .billingPeriodEnd(request.getBillingPeriodEnd())
                .previousReading(request.getPreviousReading())
                .currentReading(request.getCurrentReading())
                .quantity(quantity)
                .unitPrice(unitPrice)
                .totalAmount(totalAmount)
                .finalized(false)
                .createdBy(createdBy)
                .build();

        charge = chargeRepository.save(charge);
        log.info("Created meter reading for room service {} by user {}, amount={}", request.getRoomServiceId(), createdBy, totalAmount);
        return mapToChargeResponse(charge);
    }

    @Override
    public RoomServiceChargeResponse createServiceCharge(CreateServiceChargeRequest request, UUID createdBy) {
        RoomService roomService = roomServiceRepository.findById(request.getRoomServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dịch vụ phòng: " + request.getRoomServiceId()));

        // [P1-76] Dịch vụ không theo chỉ số
        if (roomService.getService().getBillingMethod() == BillingMethod.BY_METER) {
            throw new BusinessException("Dịch vụ tính theo chỉ số, vui lòng sử dụng nhập chỉ số");
        }

        if (request.getBillingPeriodEnd().isBefore(request.getBillingPeriodStart())) {
            throw new BusinessException("Ngày kết thúc kỳ phải sau ngày bắt đầu");
        }

        // Kiểm tra trùng kỳ
        if (chargeRepository.existsByRoomServiceIdAndBillingPeriodStartAndBillingPeriodEnd(
                request.getRoomServiceId(), request.getBillingPeriodStart(), request.getBillingPeriodEnd())) {
            throw new DuplicateResourceException("Đã có phí dịch vụ cho kỳ này");
        }

        BigDecimal unitPrice = roomService.getUnitPrice();
        BigDecimal totalAmount = request.getQuantity().multiply(unitPrice);

        RoomServiceCharge charge = RoomServiceCharge.builder()
                .roomService(roomService)
                .roomId(request.getRoomId())
                .billingPeriodStart(request.getBillingPeriodStart())
                .billingPeriodEnd(request.getBillingPeriodEnd())
                .quantity(request.getQuantity())
                .unitPrice(unitPrice)
                .totalAmount(totalAmount)
                .finalized(false)
                .createdBy(createdBy)
                .build();

        charge = chargeRepository.save(charge);
        log.info("Created service charge for room service {} by user {}, amount={}", request.getRoomServiceId(), createdBy, totalAmount);
        return mapToChargeResponse(charge);
    }

    @Override
    public RoomServiceChargeResponse finalizeCharge(UUID chargeId, UUID finalizedBy) {
        RoomServiceCharge charge = chargeRepository.findById(chargeId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phí dịch vụ: " + chargeId));

        if (charge.isFinalized()) {
            throw new BusinessException("Phí dịch vụ đã được chốt");
        }

        // [P1-88] Chốt đơn giá tại thời điểm này
        charge.setFinalized(true);
        charge = chargeRepository.save(charge);
        log.info("Finalized charge {} by user {}", chargeId, finalizedBy);
        return mapToChargeResponse(charge);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomServiceChargeResponse> getChargesByRoomAndPeriod(UUID roomId, LocalDate from, LocalDate to) {
        return chargeRepository.findByRoomIdAndPeriodRange(roomId, from, to)
                .stream().map(this::mapToChargeResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomServiceChargeResponse> getUnfinalizedCharges(UUID roomId) {
        return chargeRepository.findByRoomIdAndFinalizedFalse(roomId)
                .stream().map(this::mapToChargeResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RoomServiceChargeResponse> getChargesByRoom(UUID roomId, Pageable pageable) {
        Page<RoomServiceCharge> page = chargeRepository.findByRoomId(roomId, pageable);
        return buildPageResponse(page.map(this::mapToChargeResponse));
    }

    // ==================== Mappers ====================

    private RoomServiceChargeResponse mapToChargeResponse(RoomServiceCharge c) {
        return RoomServiceChargeResponse.builder()
                .id(c.getId())
                .roomId(c.getRoomId())
                .roomService(mapToRoomServiceResponse(c.getRoomService()))
                .billingPeriodStart(c.getBillingPeriodStart())
                .billingPeriodEnd(c.getBillingPeriodEnd())
                .previousReading(c.getPreviousReading())
                .currentReading(c.getCurrentReading())
                .quantity(c.getQuantity())
                .unitPrice(c.getUnitPrice())
                .totalAmount(c.getTotalAmount())
                .finalized(c.isFinalized())
                .createdAt(c.getCreatedAt())
                .build();
    }

    private RoomServiceResponse mapToRoomServiceResponse(RoomService rs) {
        return RoomServiceResponse.builder()
                .id(rs.getId())
                .roomId(rs.getRoomId())
                .service(ServiceResponse.builder()
                        .id(rs.getService().getId())
                        .name(rs.getService().getName())
                        .serviceType(rs.getService().getServiceType())
                        .unit(rs.getService().getUnit())
                        .billingMethod(rs.getService().getBillingMethod())
                        .defaultUnitPrice(rs.getService().getDefaultUnitPrice())
                        .status(rs.getService().getStatus())
                        .description(rs.getService().getDescription())
                        .isDefault(rs.getService().isDefault())
                        .branchId(rs.getService().getBranchId())
                        .build())
                .unitPrice(rs.getUnitPrice())
                .effectiveFrom(rs.getEffectiveFrom())
                .effectiveTo(rs.getEffectiveTo())
                .active(rs.isActive())
                .createdAt(rs.getCreatedAt())
                .build();
    }

    private <T> PageResponse<T> buildPageResponse(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
