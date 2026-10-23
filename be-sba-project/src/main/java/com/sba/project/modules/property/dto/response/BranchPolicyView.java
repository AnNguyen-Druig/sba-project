package com.sba.project.modules.property.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Effective Booking policy of a Branch (P1-50, P1-55, BR-P08/P09/P11). */
public record BranchPolicyView(
        UUID id,
        UUID branchId,
        Integer version,
        Instant effectiveFrom,
        BigDecimal bookingHoldAmount,
        Integer paymentWindowMinutes,
        Integer temporaryHoldMinutes,
        Integer confirmedHoldMinutes,
        String cancellationTerms) {
}
