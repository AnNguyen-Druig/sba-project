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
import tools.jackson.databind.JsonNode;

/**
 * Request DTO for configuring a service per room [P1-69].
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoomServiceRequest {

    @NotNull(message = "Room ID is required")
    private UUID roomId;

    @NotNull(message = "Service ID is required")
    private UUID serviceId;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Unit price must be positive")
    private BigDecimal unitPrice;

    private JsonNode billingConfig;
}
