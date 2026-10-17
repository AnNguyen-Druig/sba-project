package com.sba.project.modules.utilities.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for tenant registering an optional service under a contract [P1-71][P1-72].
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractServiceRegistrationRequest {

    @NotNull(message = "Contract ID is required")
    private UUID contractId;

    private UUID contractTenantId;

    @NotNull(message = "Room service ID is required")
    private UUID roomServiceId;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @DecimalMin(value = "0.0", inclusive = false, message = "Agreed unit price must be positive")
    private BigDecimal agreedUnitPrice;

    private String reason;
}
