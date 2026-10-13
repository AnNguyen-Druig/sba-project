package com.sba.project.modules.utilities.service.impl;

import com.sba.project.common.base.PagedResponse;
import com.sba.project.common.exceptions.AppException;
import com.sba.project.common.exceptions.ErrorCode;
import com.sba.project.modules.utilities.dto.request.ContractServiceRegistrationRequest;
import com.sba.project.modules.utilities.dto.request.CreateRoomServiceRequest;
import com.sba.project.modules.utilities.dto.request.CreateServiceRequest;
import com.sba.project.modules.utilities.dto.request.ProcessContractServiceRequest;
import com.sba.project.modules.utilities.dto.request.UpdateRoomServiceRequest;
import com.sba.project.modules.utilities.dto.request.UpdateServiceRequest;
import com.sba.project.modules.utilities.dto.response.ContractServiceResponse;
import com.sba.project.modules.utilities.dto.response.RoomServiceResponse;
import com.sba.project.modules.utilities.dto.response.ServiceCatalogResponse;
import com.sba.project.modules.utilities.entity.ContractService;
import com.sba.project.modules.utilities.entity.RoomService;
import com.sba.project.modules.utilities.entity.ServiceCatalog;
import com.sba.project.modules.utilities.enums.ContractServiceStatus;
import com.sba.project.modules.utilities.enums.RoomServiceStatus;
import com.sba.project.modules.utilities.enums.ServiceType;
import com.sba.project.modules.utilities.mapper.ContractServiceMapper;
import com.sba.project.modules.utilities.mapper.RoomServiceMapper;
import com.sba.project.modules.utilities.mapper.ServiceCatalogMapper;
import com.sba.project.modules.utilities.repository.ContractServiceRepository;
import com.sba.project.modules.utilities.repository.RoomServiceRepository;
import com.sba.project.modules.utilities.repository.ServiceCatalogRepository;
import com.sba.project.modules.utilities.service.UtilityServiceManager;
import com.sba.project.modules.utilities.specifications.ServiceCatalogSpecification;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.node.JsonNodeFactory;

/**
 * Implementation of {@link UtilityServiceManager} covering BE-04.1.
 * Handles service catalog CRUD [P1-68], per-room configuration [P1-69],
 * mandatory rule enforcement [P1-70], contract service registration/cancellation [P1-71],
 * and manager approval workflows [P1-72].
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UtilityServiceManagerImpl implements UtilityServiceManager {

    private final ServiceCatalogRepository serviceCatalogRepository;
    private final RoomServiceRepository roomServiceRepository;
    private final ContractServiceRepository contractServiceRepository;

    private final ServiceCatalogMapper serviceCatalogMapper;
    private final RoomServiceMapper roomServiceMapper;
    private final ContractServiceMapper contractServiceMapper;

    /**
     * [P1-68] Creates a new service in the branch catalog.
     * Validates that duplicate service code does not exist in the same branch.
     */
    @Override
    @Transactional
    public ServiceCatalogResponse createService(UUID branchId, CreateServiceRequest request) {
        log.info("Creating service catalog item for branchId={}, code={}", branchId, request.getCode());

        if (serviceCatalogRepository.existsByBranchIdAndCode(branchId, request.getCode())) {
            log.warn("Service code {} already exists in branch {}", request.getCode(), branchId);
            throw new AppException(ErrorCode.DATA_CONFLICT, "Service code already exists in this branch");
        }

        ServiceCatalog service = serviceCatalogMapper.toEntity(request);
        service.setBranchId(branchId);

        // Electricity and Water are mandatory by business rules [P1-70]
        boolean isMandatory = (request.getMandatory() != null)
                ? request.getMandatory()
                : (request.getServiceType() == ServiceType.ELECTRICITY || request.getServiceType() == ServiceType.WATER);
        service.setMandatory(isMandatory);

        ServiceCatalog saved = serviceCatalogRepository.save(service);
        log.info("Successfully created service id={} with code={}", saved.getId(), saved.getCode());
        return serviceCatalogMapper.toResponse(saved);
    }

    /**
     * [P1-68] Updates an existing service catalog entry.
     * Price updates only affect future billing periods [P1-77].
     */
    @Override
    @Transactional
    public ServiceCatalogResponse updateService(UUID serviceId, UpdateServiceRequest request) {
        log.info("Updating service catalog id={}", serviceId);

        ServiceCatalog service = serviceCatalogRepository.findById(serviceId)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_NOT_FOUND));

        serviceCatalogMapper.updateEntity(service, request);

        // Electricity and Water must remain mandatory [P1-70]
        if (service.getServiceType() == ServiceType.ELECTRICITY || service.getServiceType() == ServiceType.WATER) {
            service.setMandatory(true);
        }

        ServiceCatalog saved = serviceCatalogRepository.save(service);
        log.info("Successfully updated service catalog id={}", saved.getId());
        return serviceCatalogMapper.toResponse(saved);
    }

    /**
     * Retrieves service catalog details by ID.
     */
    @Override
    public ServiceCatalogResponse getService(UUID serviceId) {
        ServiceCatalog service = serviceCatalogRepository.findById(serviceId)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_NOT_FOUND));
        return serviceCatalogMapper.toResponse(service);
    }

    /**
     * [P1-68] Lists services in a branch with dynamic filtering and pagination.
     */
    @Override
    public PagedResponse<ServiceCatalogResponse> listServices(
            UUID branchId,
            ServiceType type,
            Boolean mandatory,
            Boolean active,
            Pageable pageable) {

        Specification<ServiceCatalog> spec = ServiceCatalogSpecification.withFilters(
                branchId, type, null, mandatory, active, null);

        Page<ServiceCatalog> page = serviceCatalogRepository.findAll(spec, pageable);
        List<ServiceCatalogResponse> dtoList = page.getContent().stream()
                .map(serviceCatalogMapper::toResponse)
                .toList();

        return PagedResponse.of(dtoList, pageable.getPageNumber(), pageable.getPageSize(), page.getTotalElements());
    }

    /**
     * [P1-69] Configures service pricing and billing configuration per room.
     */
    @Override
    @Transactional
    public RoomServiceResponse configureRoomService(UUID branchId, CreateRoomServiceRequest request) {
        log.info("Configuring room service for branchId={}, roomId={}, serviceId={}",
                branchId, request.getRoomId(), request.getServiceId());

        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Start date cannot be after end date");
        }

        ServiceCatalog catalog = serviceCatalogRepository.findById(request.getServiceId())
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_NOT_FOUND));

        Optional<RoomService> existing = roomServiceRepository.findByRoomIdAndServiceId(
                request.getRoomId(), request.getServiceId());
        if (existing.isPresent() && existing.get().getStatus() == RoomServiceStatus.ACTIVE) {
            throw new AppException(ErrorCode.DATA_CONFLICT, "Active service configuration already exists for this room");
        }

        RoomService roomService = roomServiceMapper.toEntity(request);
        roomService.setBranchId(branchId);
        roomService.setStatus(RoomServiceStatus.ACTIVE);

        if (roomService.getBillingConfig() == null) {
            roomService.setBillingConfig(JsonNodeFactory.instance.objectNode());
        }

        RoomService saved = roomServiceRepository.save(roomService);
        log.info("Configured room service id={} for room={}", saved.getId(), saved.getRoomId());
        return roomServiceMapper.toResponse(saved, catalog.getName());
    }

    /**
     * [P1-69] Updates room service configuration.
     * Enforces that mandatory services (electricity, water) cannot be deactivated [P1-70].
     * Price updates affect only future billing periods [P1-77].
     */
    @Override
    @Transactional
    public RoomServiceResponse updateRoomService(UUID roomServiceId, UpdateRoomServiceRequest request) {
        log.info("Updating room service configuration id={}", roomServiceId);

        RoomService roomService = roomServiceRepository.findById(roomServiceId)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_SERVICE_NOT_FOUND));

        ServiceCatalog catalog = serviceCatalogRepository.findById(roomService.getServiceId())
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_NOT_FOUND));

        // Mandatory service cannot be deactivated [P1-70]
        if (Boolean.TRUE.equals(catalog.getMandatory()) && request.getStatus() == RoomServiceStatus.INACTIVE) {
            throw new AppException(ErrorCode.MANDATORY_SERVICE_CANNOT_BE_CANCELLED,
                    "Mandatory services (electricity, water) cannot be set to inactive");
        }

        roomServiceMapper.updateEntity(roomService, request);
        RoomService saved = roomServiceRepository.save(roomService);
        log.info("Updated room service id={}", saved.getId());
        return roomServiceMapper.toResponse(saved, catalog.getName());
    }

    /**
     * Lists all service configurations for a room.
     */
    @Override
    public List<RoomServiceResponse> listRoomServices(UUID roomId) {
        List<RoomService> list = roomServiceRepository.findByRoomId(roomId);
        if (list.isEmpty()) {
            return List.of();
        }

        Map<UUID, ServiceCatalog> catalogMap = loadCatalogsForRoomServices(list);

        return list.stream()
                .map(rs -> {
                    ServiceCatalog cat = catalogMap.get(rs.getServiceId());
                    String name = cat != null ? cat.getName() : "Unknown Service";
                    return roomServiceMapper.toResponse(rs, name);
                })
                .toList();
    }

    /**
     * [P1-71] Tenant registers for an optional service on their contract.
     */
    @Override
    @Transactional
    public ContractServiceResponse registerContractService(UUID requestedBy, ContractServiceRegistrationRequest request) {
        log.info("Registering contract service: contractId={}, roomServiceId={}, requestedBy={}",
                request.getContractId(), request.getRoomServiceId(), requestedBy);

        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Start date cannot be after end date");
        }

        RoomService roomService = roomServiceRepository.findById(request.getRoomServiceId())
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_SERVICE_NOT_FOUND));

        ServiceCatalog catalog = serviceCatalogRepository.findById(roomService.getServiceId())
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_NOT_FOUND));

        BigDecimal agreedPrice = request.getAgreedUnitPrice() != null
                ? request.getAgreedUnitPrice()
                : roomService.getUnitPrice();

        ContractService cs = contractServiceMapper.toEntity(request);
        cs.setRoomId(roomService.getRoomId());
        cs.setAgreedUnitPrice(agreedPrice);
        cs.setStatus(ContractServiceStatus.PENDING);
        cs.setRequestedBy(requestedBy);

        ContractService saved = contractServiceRepository.save(cs);
        log.info("Created pending contract service registration id={}", saved.getId());
        return contractServiceMapper.toResponse(saved, catalog.getName());
    }

    /**
     * [P1-71] Tenant requests cancellation of an optional contract service.
     * Enforces that mandatory services (electricity, water) cannot be cancelled [P1-70].
     */
    @Override
    @Transactional
    public ContractServiceResponse requestCancelContractService(UUID contractServiceId, UUID requestedBy, String reason) {
        log.info("Requesting cancel for contract service id={}, requestedBy={}", contractServiceId, requestedBy);

        ContractService cs = contractServiceRepository.findById(contractServiceId)
                .orElseThrow(() -> new AppException(ErrorCode.CONTRACT_SERVICE_NOT_FOUND));

        RoomService roomService = roomServiceRepository.findById(cs.getRoomServiceId())
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_SERVICE_NOT_FOUND));

        ServiceCatalog catalog = serviceCatalogRepository.findById(roomService.getServiceId())
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_NOT_FOUND));

        // Enforce mandatory rule [P1-70]
        if (Boolean.TRUE.equals(catalog.getMandatory())
                || catalog.getServiceType() == ServiceType.ELECTRICITY
                || catalog.getServiceType() == ServiceType.WATER) {
            throw new AppException(ErrorCode.MANDATORY_SERVICE_CANNOT_BE_CANCELLED,
                    "Mandatory services (electricity, water) cannot be cancelled by tenant");
        }

        cs.setStatus(ContractServiceStatus.CANCEL_REQUESTED);
        cs.setReason(reason);

        ContractService saved = contractServiceRepository.save(cs);
        log.info("Contract service id={} marked as CANCEL_REQUESTED", saved.getId());
        return contractServiceMapper.toResponse(saved, catalog.getName());
    }

    /**
     * [P1-72] Owner or property manager approves or rejects a registration/cancellation request.
     */
    @Override
    @Transactional
    public ContractServiceResponse processContractService(
            UUID contractServiceId,
            UUID processedBy,
            ProcessContractServiceRequest request) {

        log.info("Processing contract service id={}, decision={}, processedBy={}",
                contractServiceId, request.getDecision(), processedBy);

        ContractService cs = contractServiceRepository.findById(contractServiceId)
                .orElseThrow(() -> new AppException(ErrorCode.CONTRACT_SERVICE_NOT_FOUND));

        RoomService roomService = roomServiceRepository.findById(cs.getRoomServiceId())
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_SERVICE_NOT_FOUND));

        ServiceCatalog catalog = serviceCatalogRepository.findById(roomService.getServiceId())
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_NOT_FOUND));

        if (request.getDecision() == ContractServiceStatus.APPROVED) {
            if (cs.getStatus() == ContractServiceStatus.PENDING) {
                cs.setStatus(ContractServiceStatus.APPROVED);
            } else if (cs.getStatus() == ContractServiceStatus.CANCEL_REQUESTED) {
                cs.setStatus(ContractServiceStatus.CANCELLED);
            } else {
                throw new AppException(ErrorCode.REQUEST_FAILED,
                        "Cannot approve contract service with status: " + cs.getStatus());
            }
        } else if (request.getDecision() == ContractServiceStatus.REJECTED) {
            if (cs.getStatus() == ContractServiceStatus.PENDING) {
                cs.setStatus(ContractServiceStatus.REJECTED);
            } else if (cs.getStatus() == ContractServiceStatus.CANCEL_REQUESTED) {
                // Cancellation rejected -> remains approved
                cs.setStatus(ContractServiceStatus.APPROVED);
            } else {
                throw new AppException(ErrorCode.REQUEST_FAILED,
                        "Cannot reject contract service with status: " + cs.getStatus());
            }
        } else {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Decision must be APPROVED or REJECTED");
        }

        cs.setProcessedBy(processedBy);
        cs.setProcessedAt(Instant.now());
        if (request.getReason() != null) {
            cs.setReason(request.getReason());
        }

        ContractService saved = contractServiceRepository.save(cs);
        log.info("Processed contract service id={}, new status={}", saved.getId(), saved.getStatus());
        return contractServiceMapper.toResponse(saved, catalog.getName());
    }

    /**
     * [P1-73] Lists contract services for a contract, optionally filtered by status.
     */
    @Override
    public List<ContractServiceResponse> listContractServices(UUID contractId, ContractServiceStatus status) {
        List<ContractService> list = (status != null)
                ? contractServiceRepository.findByContractIdAndStatus(contractId, status)
                : contractServiceRepository.findByContractId(contractId);

        if (list.isEmpty()) {
            return List.of();
        }

        List<UUID> roomServiceIds = list.stream().map(ContractService::getRoomServiceId).distinct().toList();
        Map<UUID, RoomService> roomServiceMap = roomServiceRepository.findAllById(roomServiceIds).stream()
                .collect(Collectors.toMap(RoomService::getId, Function.identity()));

        List<UUID> catalogIds = roomServiceMap.values().stream().map(RoomService::getServiceId).distinct().toList();
        Map<UUID, ServiceCatalog> catalogMap = serviceCatalogRepository.findAllById(catalogIds).stream()
                .collect(Collectors.toMap(ServiceCatalog::getId, Function.identity()));

        return list.stream()
                .map(cs -> {
                    RoomService rs = roomServiceMap.get(cs.getRoomServiceId());
                    String serviceName = "Unknown Service";
                    if (rs != null) {
                        ServiceCatalog cat = catalogMap.get(rs.getServiceId());
                        if (cat != null) {
                            serviceName = cat.getName();
                        }
                    }
                    return contractServiceMapper.toResponse(cs, serviceName);
                })
                .toList();
    }

    private Map<UUID, ServiceCatalog> loadCatalogsForRoomServices(List<RoomService> list) {
        List<UUID> catalogIds = list.stream().map(RoomService::getServiceId).distinct().toList();
        return serviceCatalogRepository.findAllById(catalogIds).stream()
                .collect(Collectors.toMap(ServiceCatalog::getId, Function.identity()));
    }
}
