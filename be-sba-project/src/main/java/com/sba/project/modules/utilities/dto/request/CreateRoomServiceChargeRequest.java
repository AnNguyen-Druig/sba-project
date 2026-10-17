package com.sba.project.modules.utilities.dto.request;

import com.sba.project.modules.utilities.enums.ChargeSegmentReason;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for recording a room service charge / meter reading [P1-74][P1-75].
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoomServiceChargeRequest {

    @NotNull(message = "Room service ID is required")
    private UUID roomServiceId;

    @NotNull(message = "Billing month is required")
    private LocalDate billingMonth;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotNull(message = "Segment number is required")
    @Min(value = 1, message = "Segment number must be at least 1")
    private Integer segmentNumber;

    private String meterReference;

    @NotNull(message = "Segment reason is required")
    private ChargeSegmentReason segmentReason;

    private BigDecimal startReading;

    private BigDecimal endReading;

    private BigDecimal quantity;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Unit price must be positive")
    private BigDecimal unitPrice;
}
