package com.sba.project.modules.leasing.enums;

/** Why a Booking hold ends without becoming a Contract; drives the refund table (BR Câu 9 mục B). */
public enum HoldReleaseCause {
    CUSTOMER_CANCEL,
    LANDLORD_REJECT,
    PAYMENT_WINDOW_EXPIRED,
    CONFIRMED_HOLD_EXPIRED,
    LATE_PAYMENT
}
