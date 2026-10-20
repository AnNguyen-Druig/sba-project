package com.sba.project.modules.leasing.controller;

import com.sba.project.common.base.ApiResponse;
import com.sba.project.common.base.PagedResponse;
import com.sba.project.modules.leasing.dto.response.OccupancyResponse;
import com.sba.project.modules.leasing.dto.response.TenantResponse;
import com.sba.project.modules.leasing.service.TenantDirectoryService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** BE-06.2 read-only tenant APIs. Page is 1-based; ordering is fixed (newest tenancy first). */
@RestController
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
public class TenantController {
    private final TenantDirectoryService service;

    /** P1-31/32: Owner/Manager lookup by exactly one of branchId, roomId, contractId. */
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<TenantResponse>>> search(
            @RequestParam(required = false) UUID branchId,
            @RequestParam(required = false) UUID roomId,
            @RequestParam(required = false) UUID contractId,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(ApiResponse.success(
                service.searchTenants(branchId, roomId, contractId, PageRequest.of(page - 1, size))));
    }

    /** P1-36: the caller's own tenancies. */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<PagedResponse<TenantResponse>>> myTenancies(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(ApiResponse.success(service.myTenancies(PageRequest.of(page - 1, size))));
    }

    /** P1-34 (history): the member or the room's Owner/Manager. */
    @GetMapping("/{contractTenantId}/occupancies")
    public ResponseEntity<ApiResponse<List<OccupancyResponse>>> occupancies(@PathVariable UUID contractTenantId) {
        return ResponseEntity.ok(ApiResponse.success(service.getOccupancies(contractTenantId)));
    }
}
