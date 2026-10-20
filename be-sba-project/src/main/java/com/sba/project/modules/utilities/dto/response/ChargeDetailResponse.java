package com.sba.project.modules.utilities.dto.response;

import com.sba.project.modules.utilities.enums.BillingMethod;
import com.sba.project.modules.utilities.enums.ChargeSegmentReason;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.JsonNode;

/**
 * Response DTO providing detailed breakdown of a service charge for reconciliation [P1-78].
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChargeDetailResponse {

    private UUID chargeId;
    private String serviceName;
    private LocalDate billingMonth;
    private Integer segmentNumber;
    private ChargeSegmentReason segmentReason;
    private BigDecimal startReading;
    private BigDecimal endReading;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private BillingMethod billingMethod;
    private JsonNode calculationSnapshot;
}
