package com.sba.project.service;

import com.sba.project.dto.request.*;
import com.sba.project.dto.response.*;
import com.sba.project.enums.ServiceStatus;
import com.sba.project.enums.ServiceType;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Quản lý danh mục dịch vụ (BE-04.1)
 */
public interface ServiceManagementService {

    // === Service Catalog ===
    ServiceResponse createService(CreateServiceRequest request, UUID createdBy);
    ServiceResponse updateService(UUID serviceId, UpdateServiceRequest request, UUID updatedBy);
    ServiceResponse getServiceById(UUID serviceId);
    PageResponse<ServiceResponse> getServicesByBranch(UUID branchId, ServiceStatus status, ServiceType serviceType, String keyword, Pageable pageable);
    List<ServiceResponse> getDefaultServicesByBranch(UUID branchId);

    // === Room Service Configuration ===
    RoomServiceResponse assignServiceToRoom(AssignRoomServiceRequest request, UUID assignedBy);
    RoomServiceResponse updateRoomServicePrice(UUID roomServiceId, java.math.BigDecimal newUnitPrice, UUID updatedBy);
    List<RoomServiceResponse> getRoomServices(UUID roomId);
    void deactivateRoomService(UUID roomServiceId, UUID deactivatedBy);

    // === Contract Service Registration ===
    ContractServiceResponse registerContractService(RegisterContractServiceRequest request, UUID requestedBy);
    ContractServiceResponse cancelContractServiceRegistration(UUID contractServiceId, UUID requestedBy);
    ContractServiceResponse processContractServiceRegistration(UUID contractServiceId, boolean approved, String rejectionReason, UUID processedBy);
    List<ContractServiceResponse> getContractServices(UUID contractId);
}
