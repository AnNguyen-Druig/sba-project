package com.sba.project.modules.leasing.dto.response;

import com.sba.project.modules.leasing.enums.BookingStatus;
import com.sba.project.modules.leasing.enums.HoldRefundOutcome;
import com.sba.project.modules.leasing.enums.PaymentEnvironment;
import com.sba.project.modules.leasing.enums.RentalMode;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import tools.jackson.databind.JsonNode;

/**
 * Booking read model (C07). {@code holdAmount} + {@code paymentDueAt} tell the customer what to pay
 * and by when (P1-50). {@code holdRefundOutcome} is only filled by commands that end the hold.
 */
public record BookingResponse(
        UUID id,
        UUID requesterUserId,
        UUID branchId,
        UUID roomId,
        UUID roomSlotId,
        UUID appointmentId,
        RentalMode rentalMode,
        LocalDate startDate,
        LocalDate lastDay,
        BigDecimal holdAmount,
        UUID policyId,
        JsonNode policySnapshot,
        Instant paymentDueAt,
        Instant temporaryHoldUntil,
        Instant confirmedHoldUntil,
        PaymentEnvironment paymentEnvironment,
        BookingStatus status,
        String cancellationReason,
        HoldRefundOutcome holdRefundOutcome,
        Long version,
        Instant createdAt,
        Instant updatedAt) {
}
