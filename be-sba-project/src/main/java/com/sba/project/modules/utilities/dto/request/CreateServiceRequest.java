package com.sba.project.modules.utilities.dto.request;

import com.sba.project.modules.utilities.enums.BillingMethod;
import com.sba.project.modules.utilities.enums.ServiceType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new service in the branch catalog [P1-68].
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateServiceRequest {

    @NotBlank(message = "Service code is required")
    @Size(max = 50, message = "Service code must not exceed 50 characters")
    private String code;

    @NotBlank(message = "Service name is required")
    private String name;

    @NotBlank(message = "Service unit is required")
    @Size(max = 30, message = "Service unit must not exceed 30 characters")
    private String unit;

    @NotNull(message = "Service type is required")
    private ServiceType serviceType;

    @NotNull(message = "Billing method is required")
    private BillingMethod billingMethod;

    @NotNull(message = "Reference price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Reference price must be positive")
    private BigDecimal referencePrice;

    private Boolean mandatory;
}
