package com.sba.project.modules.leasing.service.policy;

import com.sba.project.modules.leasing.enums.HoldReleaseCause;
import com.sba.project.modules.leasing.enums.HoldRefundOutcome;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

/**
 * Refund table for Booking hold deposits (br-st3-st5.md, Câu 9 mục B):
 * <pre>
 * Customer cancels within 24h of paying         -> 100%
 * Customer cancels after 24h / does not move in -> no refund
 * Landlord rejects / cannot hand over           -> 100%
 * Payment window ends with a partial payment    -> 100% of the paid part
 * Money arrives after the Booking expired       -> Manager review (restore hold if free, else 100%)
 * </pre>
 * The 24h clock starts when the money completing the deposit was received (D8, BR-P20b).
 */
public final class HoldRefundPolicy {
    public static final Duration FREE_CANCELLATION_WINDOW = Duration.ofHours(24);

    private HoldRefundPolicy() {
    }

    /**
     * @param verifiedAmount verified money allocated to the Booking (never null)
     * @param fullyPaidAt    receipt time of the payment that completed the deposit; null while unpaid/partial
     * @param releasedAt     when the hold ends
     */
    public static HoldRefundOutcome decide(HoldReleaseCause cause, BigDecimal verifiedAmount, Instant fullyPaidAt,
            Instant releasedAt) {
        if (cause == HoldReleaseCause.LATE_PAYMENT) return HoldRefundOutcome.MANAGER_REVIEW;
        if (verifiedAmount.signum() <= 0) return HoldRefundOutcome.NOTHING_PAID;
        return switch (cause) {
            case LANDLORD_REJECT -> HoldRefundOutcome.FULL_REFUND;
            case PAYMENT_WINDOW_EXPIRED -> HoldRefundOutcome.REFUND_PAID_PART;
            case CONFIRMED_HOLD_EXPIRED -> HoldRefundOutcome.NO_REFUND;
            // Cancelling before the deposit is complete happens inside the (<= 24h) payment window.
            case CUSTOMER_CANCEL -> fullyPaidAt == null ? HoldRefundOutcome.REFUND_PAID_PART
                    : !releasedAt.isAfter(fullyPaidAt.plus(FREE_CANCELLATION_WINDOW))
                            ? HoldRefundOutcome.FULL_REFUND : HoldRefundOutcome.NO_REFUND;
            case LATE_PAYMENT -> HoldRefundOutcome.MANAGER_REVIEW;
        };
    }
}
