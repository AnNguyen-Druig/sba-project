package com.sba.project.modules.utilities.controller;

import com.sba.project.common.base.ApiResponse;
import com.sba.project.modules.utilities.dto.request.CreateCostAllocationProposalRequest;
import com.sba.project.modules.utilities.dto.request.ProcessCostAllocationRequest;
import com.sba.project.modules.utilities.dto.response.CostAllocationProposalResponse;
import com.sba.project.modules.utilities.enums.CostAllocationProposalStatus;
import com.sba.project.modules.utilities.service.CostAllocationService;
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
import tools.jackson.databind.JsonNode;

/**
 * REST controller for cost allocation proposals among co-residents [P1-79][P1-80][P1-81].
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CostAllocationController {

    private final CostAllocationService costAllocationService;

    /**
     * [P1-80] Tenant proposes a cost allocation split for a service charge.
     */
    @PostMapping("/cost-allocations/propose")
    @PreAuthorize("hasAnyRole('TENANT', 'OWNER', 'MANAGER')")
    public ResponseEntity<ApiResponse<CostAllocationProposalResponse>> proposeAllocation(
            @Valid @RequestBody CreateCostAllocationProposalRequest request) {

        UUID currentUserId = SecurityUtils.getCurrentUserId();
        CostAllocationProposalResponse response = costAllocationService.proposeAllocation(currentUserId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Cost allocation proposed successfully", response));
    }

    /**
     * [P1-81] Owner or manager approves or rejects a cost allocation proposal.
     */
    @PutMapping("/cost-allocations/{proposalId}/process")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public ResponseEntity<ApiResponse<CostAllocationProposalResponse>> processProposal(
            @PathVariable UUID proposalId,
            @Valid @RequestBody ProcessCostAllocationRequest request) {

        UUID currentUserId = SecurityUtils.getCurrentUserId();
        CostAllocationProposalResponse response = costAllocationService.processProposal(proposalId, currentUserId, request);
        return ResponseEntity.ok(ApiResponse.success("Cost allocation proposal processed successfully", response));
    }

    /**
     * Retrieves a cost allocation proposal by ID.
     */
    @GetMapping("/cost-allocations/{proposalId}")
    public ResponseEntity<ApiResponse<CostAllocationProposalResponse>> getProposal(@PathVariable UUID proposalId) {
        CostAllocationProposalResponse response = costAllocationService.getProposal(proposalId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Lists cost allocation proposals for a service charge, optionally filtered by status.
     */
    @GetMapping("/service-charges/{chargeId}/allocations")
    public ResponseEntity<ApiResponse<List<CostAllocationProposalResponse>>> listProposals(
            @PathVariable UUID chargeId,
            @RequestParam(required = false) CostAllocationProposalStatus status) {

        List<CostAllocationProposalResponse> response = costAllocationService.listProposals(chargeId, status);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * [P1-79] Calculates the default resident-day cost allocation split.
     */
    @GetMapping("/service-charges/{chargeId}/default-allocation")
    public ResponseEntity<ApiResponse<JsonNode>> calculateDefaultAllocation(@PathVariable UUID chargeId) {
        JsonNode response = costAllocationService.calculateDefaultAllocation(chargeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
