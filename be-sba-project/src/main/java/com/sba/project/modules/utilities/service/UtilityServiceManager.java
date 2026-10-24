package com.sba.project.modules.utilities.service;

import com.sba.project.common.base.PagedResponse;
import com.sba.project.modules.utilities.dto.request.ContractServiceRegistrationRequest;
import com.sba.project.modules.utilities.dto.request.CreateRoomServiceRequest;
import com.sba.project.modules.utilities.dto.request.CreateServiceRequest;
import com.sba.project.modules.utilities.dto.request.ProcessContractServiceRequest;
import com.sba.project.modules.utilities.dto.request.UpdateRoomServiceRequest;
import com.sba.project.modules.utilities.dto.request.UpdateServiceRequest;
import com.sba.project.modules.utilities.dto.response.ContractServiceResponse;
import com.sba.project.modules.utilities.dto.response.RoomServiceResponse;
import com.sba.project.modules.utilities.dto.response.ServiceCatalogResponse;
import com.sba.project.modules.utilities.enums.ContractServiceStatus;
import com.sba.project.modules.utilities.enums.RoomServiceStatus;
import com.sba.project.modules.utilities.enums.ServiceType;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for managing service catalogs, per-room service assignments,
 * and contract service registrations [BE-04.1].
 */
public interface UtilityServiceManager {

    /**
     * [P1-68] Creates a new service in the branch catalog.
     */
    ServiceCatalogResponse createService(UUID branchId, CreateServiceRequest request);

    /**
     * [P1-68] Updates an existing service catalog entry.
     */
    ServiceCatalogResponse updateService(UUID serviceId, UpdateServiceRequest request);

    void deleteService(UUID serviceId);

    /**
     * Retrieves service catalog details by ID.
     */
    ServiceCatalogResponse getService(UUID serviceId);

    /**
     * [P1-68] Lists services in a branch with dynamic filtering and pagination.
     */
    PagedResponse<ServiceCatalogResponse> listServices(UUID branchId, ServiceType type, Boolean mandatory, Boolean active, Pageable pageable);

    /**
     * [P1-69] Configures service pricing and billing configuration for a specific room.
     */
    RoomServiceResponse configureRoomService(UUID branchId, CreateRoomServiceRequest request);

    /**
     * [P1-69] Updates room service configuration (pricing, status, billing config).
     */
    RoomServiceResponse updateRoomService(UUID roomServiceId, UpdateRoomServiceRequest request);

    /**
     * Lists all service configurations assigned to a specific room.
     */
    List<RoomServiceResponse> listRoomServices(UUID roomId);

    /**
     * Lists all service configurations assigned across a branch.
     */
    List<RoomServiceResponse> listBranchRoomServices(UUID branchId, RoomServiceStatus status);

    /**
     * [P1-71] Tenant registers for an optional service on a contract.
     */
    ContractServiceResponse registerContractService(UUID requestedBy, ContractServiceRegistrationRequest request);

    /**
     * [P1-71] Tenant requests cancellation of an optional contract service.
     */
    ContractServiceResponse requestCancelContractService(UUID contractServiceId, UUID requestedBy, String reason);

    /**
     * [P1-72] Owner or property manager approves or rejects a service registration/cancellation request.
     */
    ContractServiceResponse processContractService(UUID contractServiceId, UUID processedBy, ProcessContractServiceRequest request);

    /**
     * [P1-73] Lists contract services for a contract, optionally filtered by status.
     */
    List<ContractServiceResponse> listContractServices(UUID contractId, ContractServiceStatus status);

    /**
     * Lists contract services filtered by contractId, branchId, roomId, and/or status.
     */
    List<ContractServiceResponse> searchContractServices(UUID contractId, UUID branchId, UUID roomId, ContractServiceStatus status);
}
