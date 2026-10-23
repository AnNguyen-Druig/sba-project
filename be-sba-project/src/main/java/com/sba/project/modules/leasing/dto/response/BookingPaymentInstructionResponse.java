package com.sba.project.modules.leasing.dto.response;

import com.sba.project.modules.leasing.enums.PaymentEnvironment;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * P1-52: how to pay the Booking hold deposit. {@code reference} is fixed per Booking so the payment
 * module can match incoming money to it; paying does not confirm the hold until the money is verified.
 *
 * @param amountDue    hold amount minus verified money already allocated to the Booking
 * @param qrUrl        Branch payment QR; null when the Branch has not configured one
 */
public record BookingPaymentInstructionResponse(
        UUID bookingId,
        BigDecimal holdAmount,
        BigDecimal verifiedAmount,
        BigDecimal amountDue,
        String reference,
        Instant paymentDueAt,
        PaymentEnvironment environment,
        String qrUrl,
        String instructions) {
}
