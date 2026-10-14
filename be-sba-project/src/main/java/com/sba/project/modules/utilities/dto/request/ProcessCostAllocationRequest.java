package com.sba.project.modules.utilities.dto.request;

import com.sba.project.modules.utilities.enums.CostAllocationProposalStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for manager/owner approving or rejecting a cost allocation proposal [P1-81].
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessCostAllocationRequest {

    @NotNull(message = "Decision is required")
    private CostAllocationProposalStatus decision;

    private String reason;
}
