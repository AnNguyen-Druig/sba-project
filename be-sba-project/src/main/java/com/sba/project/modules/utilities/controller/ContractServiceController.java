package com.sba.project.modules.utilities.controller;

import com.sba.project.common.base.ApiResponse;
import com.sba.project.modules.utilities.dto.request.ContractServiceRegistrationRequest;
import com.sba.project.modules.utilities.dto.request.ProcessContractServiceRequest;
import com.sba.project.modules.utilities.dto.response.ContractServiceResponse;
import com.sba.project.modules.utilities.enums.ContractServiceStatus;
import com.sba.project.modules.utilities.service.UtilityServiceManager;
import com.sba.project.modules.utilities.util.SecurityUtils;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for contract optional service registrations and workflow [P1-71][P1-72][P1-73].
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ContractServiceController {

    private final UtilityServiceManager utilityServiceManager;

    /**
     * [P1-71] Tenant registers for an optional service on their rental contract.
     */
    @PostMapping("/contract-services/register")
    @PreAuthorize("hasAnyRole('TENANT', 'OWNER', 'MANAGER')")
    public ResponseEntity<ApiResponse<ContractServiceResponse>> registerContractService(
            @Valid @RequestBody ContractServiceRegistrationRequest request) {

        UUID currentUserId = SecurityUtils.getCurrentUserId();
        ContractServiceResponse response = utilityServiceManager.registerContractService(currentUserId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Service registered successfully", response));
    }

    /**
     * [P1-71] Tenant requests cancellation of an optional contract service.
     */
    @PutMapping("/contract-services/{id}/cancel")
    @PreAuthorize("hasAnyRole('TENANT', 'OWNER', 'MANAGER')")
    public ResponseEntity<ApiResponse<ContractServiceResponse>> requestCancelContractService(
            @PathVariable UUID id,
            @RequestParam(required = false) String reason) {

        UUID currentUserId = SecurityUtils.getCurrentUserId();
        ContractServiceResponse response = utilityServiceManager.requestCancelContractService(id, currentUserId, reason);
        return ResponseEntity.ok(ApiResponse.success("Cancellation requested successfully", response));
    }

    /**
     * [P1-72] Owner or manager approves or rejects a registration or cancellation request.
     */
    @PutMapping("/contract-services/{id}/process")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public ResponseEntity<ApiResponse<ContractServiceResponse>> processContractService(
            @PathVariable UUID id,
            @Valid @RequestBody ProcessContractServiceRequest request) {

        UUID currentUserId = SecurityUtils.getCurrentUserId();
        ContractServiceResponse response = utilityServiceManager.processContractService(id, currentUserId, request);
        return ResponseEntity.ok(ApiResponse.success("Contract service processed successfully", response));
    }

    /**
     * [P1-73] Lists contract services under a contract with optional status filter.
     */
    @GetMapping("/contracts/{contractId}/services")
    public ResponseEntity<ApiResponse<List<ContractServiceResponse>>> listContractServices(
            @PathVariable UUID contractId,
            @RequestParam(required = false) ContractServiceStatus status) {

        List<ContractServiceResponse> response = utilityServiceManager.listContractServices(contractId, status);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Lists contract services filtered by contractId, branchId, roomId, and/or status.
     */
    @GetMapping("/contract-services")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'STAFF', 'ADMIN', 'TENANT')")
    public ResponseEntity<ApiResponse<List<ContractServiceResponse>>> listAllContractServices(
            @RequestParam(required = false) UUID contractId,
            @RequestParam(required = false) UUID branchId,
            @RequestParam(required = false) UUID roomId,
            @RequestParam(required = false) ContractServiceStatus status) {

        List<ContractServiceResponse> response = utilityServiceManager.searchContractServices(contractId, branchId, roomId, status);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
