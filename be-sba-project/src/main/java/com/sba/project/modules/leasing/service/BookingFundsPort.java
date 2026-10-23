package com.sba.project.modules.leasing.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Integration contract implemented by Student 5 (payment). Leasing only reads verified hold money;
 * it never decides itself that a Payment is verified (P1-52/P1-53, BR-P10a).
 */
public interface BookingFundsPort {
    VerifiedHold verifiedHold(UUID bookingId);

    /**
     * @param verifiedAmount sum of {@code allocated_amount} of VERIFIED payments for the Booking; never null
     * @param fullyPaidAt    {@code received_at} of the payment that brought the total to the hold amount, or null
     */
    record VerifiedHold(BigDecimal verifiedAmount, Instant fullyPaidAt) {
        public static VerifiedHold none() {
            return new VerifiedHold(BigDecimal.ZERO, null);
        }
    }
}
