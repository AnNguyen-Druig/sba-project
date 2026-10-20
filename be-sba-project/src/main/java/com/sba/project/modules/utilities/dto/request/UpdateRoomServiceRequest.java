package com.sba.project.modules.utilities.dto.request;

import com.sba.project.modules.utilities.enums.RoomServiceStatus;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.JsonNode;

/**
 * Request DTO for updating a room service configuration [P1-69].
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRoomServiceRequest {

    @DecimalMin(value = "0.0", inclusive = false, message = "Unit price must be positive")
    private BigDecimal unitPrice;

    private LocalDate endDate;

    private RoomServiceStatus status;

    private JsonNode billingConfig;
}
