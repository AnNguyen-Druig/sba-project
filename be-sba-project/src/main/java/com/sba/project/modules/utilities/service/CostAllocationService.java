package com.sba.project.modules.utilities.service;

import com.sba.project.modules.utilities.dto.request.CreateCostAllocationProposalRequest;
import com.sba.project.modules.utilities.dto.request.ProcessCostAllocationRequest;
import com.sba.project.modules.utilities.dto.response.CostAllocationProposalResponse;
import com.sba.project.modules.utilities.enums.CostAllocationProposalStatus;
import java.util.List;
import java.util.UUID;
import tools.jackson.databind.JsonNode;

/**
 * Service interface for cost allocation proposals among co-residents [BE-04.3].
 */
public interface CostAllocationService {

    /**
     * [P1-80] Submits a proposed cost allocation split among room tenants.
     */
    CostAllocationProposalResponse proposeAllocation(UUID proposedBy, CreateCostAllocationProposalRequest request);

    /**
     * [P1-81] Approves or rejects a cost allocation proposal.
     */
    CostAllocationProposalResponse processProposal(UUID proposalId, UUID processedBy, ProcessCostAllocationRequest request);

    /**
     * Retrieves cost allocation proposal by ID.
     */
    CostAllocationProposalResponse getProposal(UUID proposalId);

    /**
     * Lists proposals for a specific service charge, optionally filtered by status.
     */
    List<CostAllocationProposalResponse> listProposals(UUID roomServiceChargeId, CostAllocationProposalStatus status);

    /**
     * [P1-79] Calculates default cost split based on resident days using the largest remainder method.
     */
    JsonNode calculateDefaultAllocation(UUID roomServiceChargeId);
}
