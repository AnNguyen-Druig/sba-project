package com.sba.project.service.impl;

import com.sba.project.dto.request.*;
import com.sba.project.dto.response.*;
import com.sba.project.entity.*;
import com.sba.project.enums.*;
import com.sba.project.exception.*;
import com.sba.project.repository.*;
import com.sba.project.service.ServiceManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ServiceManagementServiceImpl implements ServiceManagementService {

    private final ServiceRepository serviceRepository;
    private final RoomServiceRepository roomServiceRepository;
    private final ContractServiceRepository contractServiceRepository;

    // ==================== Service Catalog ====================

    @Override
    public ServiceResponse createService(CreateServiceRequest request, UUID createdBy) {
        // Kiểm tra trùng tên trong cùng Branch
        if (serviceRepository.existsByBranchIdAndName(request.getBranchId(), request.getName())) {
            throw new DuplicateResourceException("Dịch vụ '" + request.getName() + "' đã tồn tại trong Branch này");
        }

        // Xác định dịch vụ mặc định
        boolean isDefault = request.getServiceType() == ServiceType.ELECTRICITY
                         || request.getServiceType() == ServiceType.WATER;

        com.sba.project.entity.Service service = com.sba.project.entity.Service.builder()
                .name(request.getName())
                .serviceType(request.getServiceType())
                .unit(request.getUnit())
                .billingMethod(request.getBillingMethod())
                .defaultUnitPrice(request.getDefaultUnitPrice())
                .status(ServiceStatus.ACTIVE)
                .description(request.getDescription())
                .isDefault(isDefault)
                .branchId(request.getBranchId())
                .createdBy(createdBy)
                .build();

        service = serviceRepository.save(service);
        log.info("Created service '{}' for branch {} by user {}", service.getName(), request.getBranchId(), createdBy);
        return mapToServiceResponse(service);
    }

    @Override
    public ServiceResponse updateService(UUID serviceId, UpdateServiceRequest request, UUID updatedBy) {
        com.sba.project.entity.Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dịch vụ: " + serviceId));

        if (request.getName() != null && !request.getName().equals(service.getName())) {
            if (serviceRepository.existsByBranchIdAndNameAndIdNot(service.getBranchId(), request.getName(), serviceId)) {
                throw new DuplicateResourceException("Tên dịch vụ '" + request.getName() + "' đã tồn tại trong Branch");
            }
            service.setName(request.getName());
        }
        if (request.getUnit() != null) service.setUnit(request.getUnit());
        if (request.getBillingMethod() != null) service.setBillingMethod(request.getBillingMethod());
        if (request.getDefaultUnitPrice() != null) {
            // [P1-77] Thay đổi đơn giá chỉ áp dụng cho kỳ mới, không sửa hóa đơn cũ
            service.setDefaultUnitPrice(request.getDefaultUnitPrice());
        }
        if (request.getDescription() != null) service.setDescription(request.getDescription());
        if (request.getStatus() != null) service.setStatus(request.getStatus());

        service = serviceRepository.save(service);
        log.info("Updated service {} by user {}", serviceId, updatedBy);
        return mapToServiceResponse(service);
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceResponse getServiceById(UUID serviceId) {
        return serviceRepository.findById(serviceId)
                .map(this::mapToServiceResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dịch vụ: " + serviceId));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ServiceResponse> getServicesByBranch(UUID branchId, ServiceStatus status, ServiceType serviceType, String keyword, Pageable pageable) {
        Page<com.sba.project.entity.Service> page = serviceRepository.searchServices(branchId, status, serviceType, keyword, pageable);
        return buildPageResponse(page.map(this::mapToServiceResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceResponse> getDefaultServicesByBranch(UUID branchId) {
        return serviceRepository.findByBranchIdAndIsDefaultTrue(branchId)
                .stream().map(this::mapToServiceResponse).collect(Collectors.toList());
    }

    // ==================== Room Service Configuration ====================

    @Override
    public RoomServiceResponse assignServiceToRoom(AssignRoomServiceRequest request, UUID assignedBy) {
        com.sba.project.entity.Service service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dịch vụ: " + request.getServiceId()));

        if (service.getStatus() != ServiceStatus.ACTIVE) {
            throw new BusinessException("Dịch vụ đang không hoạt động");
        }

        if (roomServiceRepository.existsByRoomIdAndServiceId(request.getRoomId(), request.getServiceId())) {
            throw new DuplicateResourceException("Dịch vụ đã được gán cho phòng này");
        }

        RoomService roomService = RoomService.builder()
                .roomId(request.getRoomId())
                .service(service)
                .unitPrice(request.getUnitPrice())
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(request.getEffectiveTo())
                .active(true)
                .build();

        roomService = roomServiceRepository.save(roomService);
        log.info("Assigned service {} to room {} by user {}", request.getServiceId(), request.getRoomId(), assignedBy);
        return mapToRoomServiceResponse(roomService);
    }

    @Override
    public RoomServiceResponse updateRoomServicePrice(UUID roomServiceId, BigDecimal newUnitPrice, UUID updatedBy) {
        RoomService roomService = roomServiceRepository.findById(roomServiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cấu hình dịch vụ phòng: " + roomServiceId));

        if (newUnitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Đơn giá không được âm");
        }

        // [P1-77] Cập nhật giá chỉ cho kỳ mới
        roomService.setUnitPrice(newUnitPrice);
        roomService = roomServiceRepository.save(roomService);
        log.info("Updated room service {} price to {} by user {}", roomServiceId, newUnitPrice, updatedBy);
        return mapToRoomServiceResponse(roomService);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomServiceResponse> getRoomServices(UUID roomId) {
        return roomServiceRepository.findByRoomId(roomId)
                .stream().map(this::mapToRoomServiceResponse).collect(Collectors.toList());
    }

    @Override
    public void deactivateRoomService(UUID roomServiceId, UUID deactivatedBy) {
        RoomService roomService = roomServiceRepository.findById(roomServiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cấu hình dịch vụ phòng: " + roomServiceId));

        // [P1-70] Không cho hủy dịch vụ mặc định (điện, nước)
        if (roomService.getService().isDefault()) {
            throw new BusinessException("Không thể hủy dịch vụ mặc định (điện/nước)");
        }

        roomService.setActive(false);
        roomServiceRepository.save(roomService);
        log.info("Deactivated room service {} by user {}", roomServiceId, deactivatedBy);
    }

    // ==================== Contract Service Registration ====================

    @Override
    public ContractServiceResponse registerContractService(RegisterContractServiceRequest request, UUID requestedBy) {
        RoomService roomService = roomServiceRepository.findById(request.getRoomServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dịch vụ phòng: " + request.getRoomServiceId()));

        // [P1-70] Không cho đăng ký lại dịch vụ mặc định (đã tự động có)
        if (roomService.getService().isDefault()) {
            throw new BusinessException("Dịch vụ mặc định (điện/nước) đã được tự động đăng ký");
        }

        // Kiểm tra đăng ký trùng
        List<ServiceRegistrationStatus> activeStatuses = List.of(
            ServiceRegistrationStatus.PENDING, ServiceRegistrationStatus.APPROVED
        );
        if (contractServiceRepository.existsByContractIdAndRoomServiceIdAndRegistrationStatusIn(
                request.getContractId(), request.getRoomServiceId(), activeStatuses)) {
            throw new DuplicateResourceException("Dịch vụ đã được đăng ký cho hợp đồng này");
        }

        ContractService contractService = ContractService.builder()
                .contractId(request.getContractId())
                .roomService(roomService)
                .registrationStatus(ServiceRegistrationStatus.PENDING)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .requestedBy(requestedBy)
                .build();

        contractService = contractServiceRepository.save(contractService);
        log.info("Contract service registration created for contract {} by tenant {}", request.getContractId(), requestedBy);
        return mapToContractServiceResponse(contractService);
    }

    @Override
    public ContractServiceResponse cancelContractServiceRegistration(UUID contractServiceId, UUID requestedBy) {
        ContractService cs = contractServiceRepository.findById(contractServiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đăng ký dịch vụ: " + contractServiceId));

        // [P1-70] Không cho Tenant tự hủy dịch vụ điện/nước mặc định
        if (cs.getRoomService().getService().isDefault()) {
            throw new BusinessException("Không thể hủy dịch vụ mặc định (điện/nước)");
        }

        if (cs.getRegistrationStatus() == ServiceRegistrationStatus.CANCELLED) {
            throw new BusinessException("Đăng ký dịch vụ đã bị hủy");
        }

        cs.setRegistrationStatus(ServiceRegistrationStatus.CANCELLED);
        cs.setProcessedBy(requestedBy);
        cs.setProcessedAt(java.time.LocalDateTime.now());
        cs = contractServiceRepository.save(cs);
        log.info("Contract service {} cancelled by {}", contractServiceId, requestedBy);
        return mapToContractServiceResponse(cs);
    }

    @Override
    public ContractServiceResponse processContractServiceRegistration(UUID contractServiceId, boolean approved, String rejectionReason, UUID processedBy) {
        ContractService cs = contractServiceRepository.findById(contractServiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đăng ký dịch vụ: " + contractServiceId));

        if (cs.getRegistrationStatus() != ServiceRegistrationStatus.PENDING) {
            throw new BusinessException("Chỉ có thể xử lý đăng ký đang chờ duyệt");
        }

        cs.setRegistrationStatus(approved ? ServiceRegistrationStatus.APPROVED : ServiceRegistrationStatus.REJECTED);
        cs.setProcessedBy(processedBy);
        cs.setProcessedAt(java.time.LocalDateTime.now());
        if (!approved) {
            cs.setRejectionReason(rejectionReason);
        }

        cs = contractServiceRepository.save(cs);
        log.info("Contract service {} {} by {}", contractServiceId, approved ? "approved" : "rejected", processedBy);
        return mapToContractServiceResponse(cs);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractServiceResponse> getContractServices(UUID contractId) {
        return contractServiceRepository.findByContractId(contractId)
                .stream().map(this::mapToContractServiceResponse).collect(Collectors.toList());
    }

    // ==================== Mappers ====================

    private ServiceResponse mapToServiceResponse(com.sba.project.entity.Service s) {
        return ServiceResponse.builder()
                .id(s.getId())
                .name(s.getName())
                .serviceType(s.getServiceType())
                .unit(s.getUnit())
                .billingMethod(s.getBillingMethod())
                .defaultUnitPrice(s.getDefaultUnitPrice())
                .status(s.getStatus())
                .description(s.getDescription())
                .isDefault(s.isDefault())
                .branchId(s.getBranchId())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }

    private RoomServiceResponse mapToRoomServiceResponse(RoomService rs) {
        return RoomServiceResponse.builder()
                .id(rs.getId())
                .roomId(rs.getRoomId())
                .service(mapToServiceResponse(rs.getService()))
                .unitPrice(rs.getUnitPrice())
                .effectiveFrom(rs.getEffectiveFrom())
                .effectiveTo(rs.getEffectiveTo())
                .active(rs.isActive())
                .createdAt(rs.getCreatedAt())
                .build();
    }

    private ContractServiceResponse mapToContractServiceResponse(ContractService cs) {
        return ContractServiceResponse.builder()
                .id(cs.getId())
                .contractId(cs.getContractId())
                .roomService(mapToRoomServiceResponse(cs.getRoomService()))
                .registrationStatus(cs.getRegistrationStatus())
                .startDate(cs.getStartDate())
                .endDate(cs.getEndDate())
                .requestedBy(cs.getRequestedBy())
                .processedBy(cs.getProcessedBy())
                .processedAt(cs.getProcessedAt())
                .rejectionReason(cs.getRejectionReason())
                .createdAt(cs.getCreatedAt())
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
