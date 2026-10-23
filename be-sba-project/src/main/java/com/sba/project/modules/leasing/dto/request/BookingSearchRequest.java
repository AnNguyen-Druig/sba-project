package com.sba.project.modules.leasing.dto.request;

import com.sba.project.modules.leasing.enums.BookingStatus;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Owner/Manager Booking list (P1-51). {@code branchId} is required and checked against the caller's
 * scope; {@code from}/{@code to} filter by stay period overlap (inclusive days).
 */
public record BookingSearchRequest(
        UUID branchId,
        UUID roomId,
        BookingStatus status,
        LocalDate from,
        LocalDate to) {
}
