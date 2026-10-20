package com.sba.project.modules.utilities.dto.request;

import com.sba.project.modules.utilities.enums.AllocationMethod;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.JsonNode;

/**
 * Request DTO for tenant submitting a cost allocation proposal [P1-80].
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCostAllocationProposalRequest {

    @NotNull(message = "Room service charge ID is required")
    private UUID roomServiceChargeId;

    @NotNull(message = "Allocation method is required")
    private AllocationMethod method;

    @NotNull(message = "Allocation details JSON is required")
    private JsonNode allocationDetails;

    private String reason;
}
