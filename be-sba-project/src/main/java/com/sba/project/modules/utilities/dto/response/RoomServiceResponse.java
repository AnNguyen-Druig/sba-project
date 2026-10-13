package com.sba.project.modules.utilities.dto.response;

import com.sba.project.modules.utilities.enums.RoomServiceStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.JsonNode;

/**
 * Response DTO representing a room service configuration [P1-69].
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomServiceResponse {

    private UUID id;
    private UUID roomId;
    private UUID branchId;
    private UUID serviceId;
    private String serviceName;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal unitPrice;
    private JsonNode billingConfig;
    private RoomServiceStatus status;
}
