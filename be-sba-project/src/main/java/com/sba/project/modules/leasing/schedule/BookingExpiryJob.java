package com.sba.project.modules.leasing.schedule;

import com.sba.project.modules.leasing.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * P1-55 background job: settles Booking holds past their payment or confirmed-hold deadline so the
 * Room/Slot is released (BR-P10a, BR-P11). Holds of a Room are also settled immediately when someone
 * requests it, so this job only bounds how long an untouched lapsed hold stays visible.
 */
@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class BookingExpiryJob {
    private final BookingService bookingService;

    @Scheduled(fixedDelayString = "${app.leasing.booking-expiry-interval-ms:60000}",
            initialDelayString = "${app.leasing.booking-expiry-initial-delay-ms:60000}")
    public void settleLapsedHolds() {
        try {
            int settled = bookingService.settleLapsedHolds();
            if (settled > 0) log.info("Settled {} lapsed Booking holds", settled);
        } catch (RuntimeException ex) {
            // Never let one failed run cancel the schedule; the next tick retries.
            log.warn("Booking hold expiry run failed ({})", ex.getClass().getSimpleName());
        }
    }
}
