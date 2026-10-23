package com.sba.project.modules.leasing.service.impl;

import com.sba.project.modules.leasing.service.BookingFundsPort;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** TEMPORARY fail-closed placeholder until Student 5's payment verification is merged: nothing is paid. */
@Component
public class PendingPaymentBookingFundsPort implements BookingFundsPort {
    @Override
    public VerifiedHold verifiedHold(UUID bookingId) {
        return VerifiedHold.none();
    }
}
