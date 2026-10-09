package com.sba.project.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomServiceChargeResponse {
    private UUID id;
    private UUID roomId;
    private RoomServiceResponse roomService;
    private LocalDate billingPeriodStart;
    private LocalDate billingPeriodEnd;
    private BigDecimal previousReading;
    private BigDecimal currentReading;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private boolean finalized;
    private LocalDateTime createdAt;
}
