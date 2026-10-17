package com.sba.project.modules.utilities.dto.request;

import com.sba.project.modules.utilities.enums.BillingMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating an existing service in the catalog [P1-68].
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateServiceRequest {

    private String name;

    @Size(max = 30, message = "Service unit must not exceed 30 characters")
    private String unit;

    private BillingMethod billingMethod;

    @DecimalMin(value = "0.0", inclusive = false, message = "Reference price must be positive")
    private BigDecimal referencePrice;

    private Boolean active;
}
