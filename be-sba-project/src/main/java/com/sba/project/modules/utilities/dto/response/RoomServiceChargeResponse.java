package com.sba.project.modules.utilities.dto.response;

import com.sba.project.modules.utilities.enums.ChargeSegmentReason;
import com.sba.project.modules.utilities.enums.RoomServiceChargeStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.JsonNode;

/**
 * Response DTO representing a room service charge record [P1-74][P1-75].
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomServiceChargeResponse {

    private UUID id;
    private UUID roomServiceId;
    private UUID roomId;
    private String serviceName;
    private LocalDate billingMonth;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer segmentNumber;
    private String meterReference;
    private ChargeSegmentReason segmentReason;
    private BigDecimal startReading;
    private BigDecimal endReading;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private JsonNode calculationSnapshot;
    private JsonNode evidenceRefs;
    private RoomServiceChargeStatus status;
    private UUID finalizedBy;
    private Instant finalizedAt;
}
