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
@RestController
@RequestMapping("/api/v1/cost-allocations")
@RequiredArgsConstructor
public class CostAllocationController {

    private final CostAllocationService costAllocationService;

    @PostMapping
    public ResponseEntity<CostAllocationResponse> createProposal(
            @Valid @RequestBody CreateCostAllocationRequest request,
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(costAllocationService.createProposal(request, userId));
    }

    @PatchMapping("/{proposalId}/process")

    public ResponseEntity<CostAllocationResponse> processProposal(
            @PathVariable UUID proposalId,
            @Valid @RequestBody ProcessAllocationRequest request,
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(costAllocationService.processProposal(proposalId, request, userId));
    }

    @GetMapping("/rooms/{roomId}")

    public ResponseEntity<PageResponse<CostAllocationResponse>> getProposalsByRoom(
            @PathVariable UUID roomId,
            @RequestParam(required = false) AllocationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(costAllocationService.getProposalsByRoom(roomId, status, pageable));
    }

    @GetMapping("/tenants/{tenantId}")

    public ResponseEntity<List<CostAllocationResponse>> getProposalsByTenant(@PathVariable UUID tenantId) {
        return ResponseEntity.ok(costAllocationService.getProposalsByTenant(tenantId));
    }

    @GetMapping("/charges/{chargeId}")

    public ResponseEntity<List<CostAllocationResponse>> getProposalsByCharge(@PathVariable UUID chargeId) {
        return ResponseEntity.ok(costAllocationService.getProposalsByCharge(chargeId));
    }

    @PostMapping("/charges/{chargeId}/default")

    public ResponseEntity<CostAllocationResponse> createDefaultAllocation(
            @PathVariable UUID chargeId,
            @RequestBody List<UUID> tenantIds,
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(costAllocationService.createDefaultAllocation(chargeId, tenantIds, userId));
    }
}
