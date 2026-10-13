package com.sba.project.modules.utilities.dto.request;

import com.sba.project.modules.utilities.enums.ContractServiceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for owner/manager processing (approve/reject) a contract service registration or cancellation [P1-72].
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessContractServiceRequest {

    @NotNull(message = "Decision is required")
    private ContractServiceStatus decision;

    private String reason;
}
