package com.sba.project.modules.utilities.dto.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for finalizing a batch of room service charges.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinalizeChargeRequest {

    @NotEmpty(message = "Charge IDs list must not be empty")
    private List<UUID> chargeIds;
}
