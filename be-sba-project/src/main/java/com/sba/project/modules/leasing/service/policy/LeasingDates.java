package com.sba.project.modules.leasing.service.policy;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

/**
 * Date conventions shared by Booking, Contract and Occupancy.
 * <ul>
 *   <li>Business rules count both ends (BR-G01) and the hand-over day is a stay day (BR-G04).</li>
 *   <li>The schema stores {@code [start_date, end_date)} with {@code end_date > start_date}, so
 *       {@code end_date} = last stay day + 1. APIs speak in {@code lastDay}; only this class converts.</li>
 *   <li>"Today" and day deadlines use Vietnam time (BR-P03a), never the server default zone.</li>
 * </ul>
 */
public final class LeasingDates {
    public static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private LeasingDates() {
    }

    public static LocalDate today(Clock clock) {
        return LocalDate.now(clock.withZone(ZONE));
    }

    /** Inclusive last stay day -> exclusive {@code end_date} column value. */
    public static LocalDate toEndExclusive(LocalDate lastDay) {
        return lastDay.plusDays(1);
    }

    /** Exclusive {@code end_date} column value -> inclusive last stay day. */
    public static LocalDate toLastDay(LocalDate endExclusive) {
        return endExclusive == null ? null : endExclusive.minusDays(1);
    }

    /** BR-G01: number of stay days, both ends included. */
    public static long stayDays(LocalDate firstDay, LocalDate lastDay) {
        return ChronoUnit.DAYS.between(firstDay, lastDay) + 1;
    }
}
