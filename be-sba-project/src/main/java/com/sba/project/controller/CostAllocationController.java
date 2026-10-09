package com.sba.project.controller;

import com.sba.project.dto.request.*;
import com.sba.project.dto.response.*;
import com.sba.project.enums.AllocationStatus;
import com.sba.project.service.CostAllocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * API phân bổ chi phí dịch vụ cho người ở ghép.
 * [BE-04.3] Student 4
 */
@RestController
@RequestMapping("/api/v1/cost-allocations")
@RequiredArgsConstructor
public class CostAllocationController {

    private final CostAllocationService costAllocationService;

    /**
     * [P1-80] Tenant tạo đề xuất phân bổ chi phí
     */
    @PostMapping
    public ResponseEntity<CostAllocationResponse> createProposal(
            @Valid @RequestBody CreateCostAllocationRequest request,
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(costAllocationService.createProposal(request, userId));
    }

    /**
     * [P1-81] Manager/Owner duyệt hoặc từ chối đề xuất
     */
    @PatchMapping("/{proposalId}/process")
    public ResponseEntity<CostAllocationResponse> processProposal(
            @PathVariable UUID proposalId,
            @Valid @RequestBody ProcessAllocationRequest request,
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(costAllocationService.processProposal(proposalId, request, userId));
    }

    /**
     * [P1-83] Xem danh sách đề xuất theo phòng
     */
    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<PageResponse<CostAllocationResponse>> getProposalsByRoom(
            @PathVariable UUID roomId,
            @RequestParam(required = false) AllocationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(costAllocationService.getProposalsByRoom(roomId, status, pageable));
    }

    /**
     * [P1-83] Xem đề xuất theo Tenant
     */
    @GetMapping("/tenants/{tenantId}")
    public ResponseEntity<List<CostAllocationResponse>> getProposalsByTenant(@PathVariable UUID tenantId) {
        return ResponseEntity.ok(costAllocationService.getProposalsByTenant(tenantId));
    }

    /**
     * Xem đề xuất theo charge
     */
    @GetMapping("/charges/{chargeId}")
    public ResponseEntity<List<CostAllocationResponse>> getProposalsByCharge(@PathVariable UUID chargeId) {
        return ResponseEntity.ok(costAllocationService.getProposalsByCharge(chargeId));
    }

    /**
     * [P1-79] Tạo phân bổ mặc định (chia đều) cho một phí dịch vụ
     */
    @PostMapping("/charges/{chargeId}/default")
    public ResponseEntity<CostAllocationResponse> createDefaultAllocation(
            @PathVariable UUID chargeId,
            @RequestBody List<UUID> tenantIds,
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(costAllocationService.createDefaultAllocation(chargeId, tenantIds, userId));
    }
}
