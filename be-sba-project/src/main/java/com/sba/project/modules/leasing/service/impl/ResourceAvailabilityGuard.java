package com.sba.project.modules.leasing.service.impl;

import com.sba.project.common.exceptions.AppException;
import com.sba.project.common.exceptions.ErrorCode;
import com.sba.project.modules.leasing.entity.Booking;
import com.sba.project.modules.leasing.enums.BookingStatus;
import com.sba.project.modules.leasing.enums.HoldReleaseCause;
import com.sba.project.modules.leasing.repository.BookingRepository;
import com.sba.project.modules.leasing.repository.ContractTenantRepository;
import com.sba.project.modules.leasing.repository.ResourceAllocationRepository;
import com.sba.project.modules.leasing.service.policy.LeasingDates;
import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Single place that decides whether a Room/Slot is free for {@code [start, endExclusive)}; shared by
 * Booking and Contract (P1-54, P1-58, P1-62, BR-P10/P10b, BR-R07, BR-R20b). Must run inside the
 * transaction that writes the new hold/lease, so the Room lock covers check + insert. The deferred
 * leasing_integrity trigger remains the last line of defence at commit.
 */
@Component
@RequiredArgsConstructor
class ResourceAvailabilityGuard {
    private final ResourceAllocationRepository allocationRepository;
    private final BookingRepository bookingRepository;
    private final ContractTenantRepository contractTenantRepository;
    private final BookingHoldReleaser holdReleaser;
    private final Clock clock;

    /** @param slotId null for a whole-room request */
    void lockAndAssertFree(UUID roomId, UUID slotId, LocalDate start, LocalDate endExclusive) {
        allocationRepository.lockRoom(roomId.toString());
        expireStaleHolds(roomId);
        LocalDate today = LeasingDates.today(clock);
        boolean taken = slotId == null
                ? allocationRepository.existsLiveInRoom(roomId, start, endExclusive)
                        || contractTenantRepository.existsOverstayInRoom(roomId, today)
                : allocationRepository.existsLiveForSlot(roomId, slotId, start, endExclusive)
                        || contractTenantRepository.existsOverstayForSlot(roomId, slotId, today);
        if (taken) throw new AppException(ErrorCode.RESOURCE_UNAVAILABLE);
    }

    /**
     * The overlap trigger ignores {@code expires_at}, so a lapsed hold blocks the Room until it is
     * RELEASED. Release them here instead of waiting for the scheduled job (BR-P10a, BR-P11).
     */
    private void expireStaleHolds(UUID roomId) {
        for (Booking stale : bookingRepository.findStaleHoldsInRoom(roomId, clock.instant())) {
            HoldReleaseCause cause = stale.getStatus() == BookingStatus.CONFIRMED
                    ? HoldReleaseCause.CONFIRMED_HOLD_EXPIRED : HoldReleaseCause.PAYMENT_WINDOW_EXPIRED;
            holdReleaser.release(stale, BookingStatus.EXPIRED, cause, null);
        }
    }
}
