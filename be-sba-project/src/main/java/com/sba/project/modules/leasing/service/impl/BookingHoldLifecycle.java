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
import com.sba.project.modules.leasing.service.BookingFundsPort.VerifiedHold;
import com.sba.project.modules.leasing.service.policy.HoldRefundPolicy;
import com.sba.project.modules.property.service.PropertyQueryService;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * State changes of a live Booking hold, kept in one place so the API, the expiry job and the
 * availability guard apply the same rules (BR-P09 → BR-P11). Every method runs in the caller's
 * transaction and expects the caller to hold the Booking row lock.
 * <p>
 * The status history (P1-58) is written by the database audit_change() trigger; the bound reason
 * records why and the refund decision (D4). Each step flushes because the trigger reads the reason
 * at write time, not at commit.
 */
@Component
@RequiredArgsConstructor
class BookingHoldLifecycle {
    static final Set<BookingStatus> LIVE_HOLD = EnumSet.of(BookingStatus.AWAITING_PAYMENT, BookingStatus.CONFIRMED);

    private final BookingRepository bookingRepository;
    private final ResourceAllocationRepository allocationRepository;
    private final BookingFundsPort fundsPort;
    private final PropertyQueryService propertyQueryService;
    private final AuditActorContext auditContext;
    private final Clock clock;

    /**
     * The deposit counts only when verified money covers the hold amount and was received by the
     * payment deadline (BR-P10a, BR-P10c). Receipt time, not verification time, decides (BR-P20b).
     */
    static boolean isFullyPaidOnTime(Booking booking, VerifiedHold funds) {
        return funds.fullyPaidAt() != null
                && funds.verifiedAmount().compareTo(booking.getHoldAmount()) >= 0
                && !funds.fullyPaidAt().isAfter(booking.getPaymentDueAt());
    }

    /** Past its temporary or confirmed hold deadline. */
    static boolean isLapsed(Booking booking, Instant now) {
        return switch (booking.getStatus()) {
            case AWAITING_PAYMENT -> booking.getTemporaryHoldUntil().isBefore(now);
            case CONFIRMED -> booking.getConfirmedHoldUntil().isBefore(now);
            default -> false;
        };
    }

    /**
     * AWAITING_PAYMENT → CONFIRMED (P1-53). The hold is kept for the confirmed period of the policy
     * frozen on the Booking (BR-P11: 14 days from full deposit), counted from now.
     */
    void confirm(Booking booking) {
        Instant now = clock.instant();
        int confirmedMinutes = propertyQueryService.getPolicy(booking.getPolicyId()).confirmedHoldMinutes();
        Instant confirmedUntil = now.plus(Duration.ofMinutes(confirmedMinutes));

        auditContext.bindReason("HOLD_CONFIRMED");
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setConfirmedHoldUntil(confirmedUntil);
        booking.setVersion(booking.getVersion() + 1);
        bookingRepository.saveAndFlush(booking);
        for (ResourceAllocation allocation : liveAllocations(booking)) {
            allocation.setExpiresAt(confirmedUntil);
            allocationRepository.saveAndFlush(allocation);
        }
        auditContext.bindReason(null);
    }

    /**
     * A hold past its deadline (P1-55). Money that arrived in time but was verified late still
     * confirms the hold; otherwise the Booking expires and the Room/Slot is released.
     */
    void settleLapsed(Booking booking) {
        VerifiedHold funds = fundsPort.verifiedHold(booking.getId());
        if (booking.getStatus() == BookingStatus.AWAITING_PAYMENT) {
            if (isFullyPaidOnTime(booking, funds)) {
                confirm(booking);
                return;
            }
            release(booking, BookingStatus.EXPIRED, HoldReleaseCause.PAYMENT_WINDOW_EXPIRED, null, funds);
            return;
        }
        release(booking, BookingStatus.EXPIRED, HoldReleaseCause.CONFIRMED_HOLD_EXPIRED, null, funds);
    }

    /** Ends a live hold: terminal status + RELEASED allocation, as the leasing trigger requires. */
    HoldRefundOutcome release(Booking booking, BookingStatus terminal, HoldReleaseCause cause, String reason) {
        return release(booking, terminal, cause, reason, fundsPort.verifiedHold(booking.getId()));
    }

    private HoldRefundOutcome release(Booking booking, BookingStatus terminal, HoldReleaseCause cause,
            String reason, VerifiedHold funds) {
        Instant now = clock.instant();
        HoldRefundOutcome outcome = HoldRefundPolicy.decide(cause, funds.verifiedAmount(), funds.fullyPaidAt(), now);

        auditContext.bindReason(cause + "; refund=" + outcome + (reason == null ? "" : "; " + reason));
        booking.setStatus(terminal);
        if (terminal == BookingStatus.CANCELLED || terminal == BookingStatus.REJECTED) {
            booking.setCancellationReason(reason);
        }
        booking.setVersion(booking.getVersion() + 1);
        bookingRepository.saveAndFlush(booking);
        for (ResourceAllocation allocation : liveAllocations(booking)) {
            allocation.setStatus(ResourceAllocationStatus.RELEASED);
            allocation.setReleasedAt(now);
            allocationRepository.saveAndFlush(allocation);
        }
        auditContext.bindReason(null);
        return outcome;
    }

    private Iterable<ResourceAllocation> liveAllocations(Booking booking) {
        return allocationRepository.findByBookingIdAndStatusNot(booking.getId(), ResourceAllocationStatus.RELEASED);
    }
}
