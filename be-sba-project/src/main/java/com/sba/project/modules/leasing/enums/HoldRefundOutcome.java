package com.sba.project.modules.leasing.enums;

/**
 * Policy decision for a Booking hold deposit. It is a decision only: approval and the actual refund
 * are recorded by the payment module (BR-P17a–P17c).
 */
public enum HoldRefundOutcome {
    NOTHING_PAID,
    FULL_REFUND,
    NO_REFUND,
    REFUND_PAID_PART,
    MANAGER_REVIEW
}
