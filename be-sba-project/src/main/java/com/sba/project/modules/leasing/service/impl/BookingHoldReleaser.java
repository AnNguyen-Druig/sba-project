package com.sba.project.modules.leasing.service.impl;

import com.sba.project.common.audit.AuditActorContext;
import com.sba.project.modules.leasing.entity.Booking;
import com.sba.project.modules.leasing.entity.ResourceAllocation;
import com.sba.project.modules.leasing.enums.BookingStatus;
import com.sba.project.modules.leasing.enums.HoldReleaseCause;
import com.sba.project.modules.leasing.enums.HoldRefundOutcome;
import com.sba.project.modules.leasing.enums.ResourceAllocationStatus;
import com.sba.project.modules.leasing.repository.BookingRepository;
import com.sba.project.modules.leasing.repository.ResourceAllocationRepository;
import com.sba.project.modules.leasing.service.BookingFundsPort;
import com.sba.project.modules.leasing.service.policy.HoldRefundPolicy;
import java.time.Clock;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Ends a live Booking hold: terminal status + RELEASED allocation in the caller's transaction, as the
 * leasing trigger requires. The status history (P1-58) is written by the database audit_change()
 * trigger; the bound reason carries the refund decision (D4).
 */
@Component
@RequiredArgsConstructor
class BookingHoldReleaser {
    static final Set<BookingStatus> LIVE_HOLD = EnumSet.of(BookingStatus.AWAITING_PAYMENT, BookingStatus.CONFIRMED);

    private final BookingRepository bookingRepository;
    private final ResourceAllocationRepository allocationRepository;
    private final BookingFundsPort fundsPort;
    private final AuditActorContext auditContext;
    private final Clock clock;

    /** Caller must hold the Booking row lock or the Room lock. */
    HoldRefundOutcome release(Booking booking, BookingStatus terminal, HoldReleaseCause cause, String reason) {
        Instant now = clock.instant();
        BookingFundsPort.VerifiedHold funds = fundsPort.verifiedHold(booking.getId());
        HoldRefundOutcome outcome = HoldRefundPolicy.decide(cause, funds.verifiedAmount(), funds.fullyPaidAt(), now);

        // Flush per step: audit_change() reads rental.reason at write time, not at commit.
        auditContext.bindReason(cause + "; refund=" + outcome + (reason == null ? "" : "; " + reason));
        booking.setStatus(terminal);
        if (terminal == BookingStatus.CANCELLED || terminal == BookingStatus.REJECTED) {
            booking.setCancellationReason(reason);
        }
        booking.setVersion(booking.getVersion() + 1);
        bookingRepository.saveAndFlush(booking);
        for (ResourceAllocation allocation : allocationRepository.findByBookingIdAndStatusNot(booking.getId(),
                ResourceAllocationStatus.RELEASED)) {
            allocation.setStatus(ResourceAllocationStatus.RELEASED);
            allocation.setReleasedAt(now);
            allocationRepository.saveAndFlush(allocation);
        }
        auditContext.bindReason(null);
        return outcome;
    }
}
