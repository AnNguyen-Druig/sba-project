package com.sba.project.modules.utilities.dto.response;

import com.sba.project.modules.utilities.enums.AllocationMethod;
import com.sba.project.modules.utilities.enums.CostAllocationProposalStatus;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.JsonNode;

/**
 * Response DTO representing a cost allocation proposal [P1-80][P1-81].
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CostAllocationProposalResponse {

    private UUID id;
    private UUID roomServiceChargeId;
    private AllocationMethod method;
    private JsonNode allocationDetails;
    private JsonNode calculationSnapshot;
    private String reason;
    private UUID proposedBy;
    private CostAllocationProposalStatus status;
    private UUID processedBy;
    private Instant processedAt;
    private Instant createdAt;
}
