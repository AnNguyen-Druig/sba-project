package com.sba.project.modules.utilities.controller;

import com.sba.project.common.base.ApiResponse;
import com.sba.project.common.base.PagedResponse;
import com.sba.project.modules.utilities.dto.request.CreateServiceRequest;
import com.sba.project.modules.utilities.dto.request.UpdateServiceRequest;
import com.sba.project.modules.utilities.dto.response.ServiceCatalogResponse;
import com.sba.project.modules.utilities.enums.ServiceType;
import com.sba.project.modules.utilities.service.UtilityServiceManager;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for service catalog management [P1-68].
 * Security authorizations are configured in alignment with Student 1 (Auth module).
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ServiceCatalogController {

    private final UtilityServiceManager utilityServiceManager;

    /**
     * [P1-68] Creates a new service catalog entry for a branch.
     */
    @PostMapping("/branches/{branchId}/services")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<ServiceCatalogResponse>> createService(
            @PathVariable UUID branchId,
            @Valid @RequestBody CreateServiceRequest request) {

        ServiceCatalogResponse response = utilityServiceManager.createService(branchId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Service created successfully", response));
    }

    /**
     * [P1-68] Updates an existing service catalog entry.
     */
    @PutMapping("/services/{serviceId}")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<ServiceCatalogResponse>> updateService(
            @PathVariable UUID serviceId,
            @Valid @RequestBody UpdateServiceRequest request) {

        ServiceCatalogResponse response = utilityServiceManager.updateService(serviceId, request);
        return ResponseEntity.ok(ApiResponse.success("Service updated successfully", response));
    }

    /**
     * Retrieves a service catalog entry by ID.
     */
    /**
     * [P1-68] Deletes a service catalog entry if not referenced in room configurations.
     */
    @DeleteMapping("/services/{serviceId}")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<Void>> deleteService(@PathVariable UUID serviceId) {
        utilityServiceManager.deleteService(serviceId);
        return ResponseEntity.ok(ApiResponse.success("Service deleted successfully", null));
    }

    @GetMapping("/services/{serviceId}")
    public ResponseEntity<ApiResponse<ServiceCatalogResponse>> getService(@PathVariable UUID serviceId) {
        ServiceCatalogResponse response = utilityServiceManager.getService(serviceId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * [P1-68] Lists services in a branch with optional filters and pagination.
     */
    @GetMapping("/branches/{branchId}/services")
    public ResponseEntity<ApiResponse<PagedResponse<ServiceCatalogResponse>>> listServices(
            @PathVariable UUID branchId,
            @RequestParam(required = false) ServiceType type,
            @RequestParam(required = false) Boolean mandatory,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20, sort = "code", direction = Sort.Direction.ASC) Pageable pageable) {

        PagedResponse<ServiceCatalogResponse> response = utilityServiceManager
                .listServices(branchId, type, mandatory, active, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
