package com.sba.project.modules.leasing.specifications;

import com.sba.project.modules.leasing.entity.Booking;
import com.sba.project.modules.leasing.enums.BookingStatus;
import com.sba.project.modules.leasing.service.policy.LeasingDates;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/** Booking filters for Owner/Manager lists and the customer's own list (P1-51). */
public final class BookingSpecifications {
    private BookingSpecifications() {
    }

    public static Specification<Booking> forBranch(UUID branchId, UUID roomId, BookingStatus status,
            LocalDate from, LocalDate to) {
        List<Specification<Booking>> filters = new ArrayList<>();
        filters.add(equal("branchId", branchId));
        if (roomId != null) filters.add(equal("roomId", roomId));
        if (status != null) filters.add(equal("status", status));
        if (from != null) filters.add(endsAfter(from));
        if (to != null) filters.add(startsOnOrBefore(to));
        return Specification.allOf(filters);
    }

    public static Specification<Booking> forRequester(UUID requesterUserId, BookingStatus status) {
        List<Specification<Booking>> filters = new ArrayList<>();
        filters.add(equal("requesterUserId", requesterUserId));
        if (status != null) filters.add(equal("status", status));
        return Specification.allOf(filters);
    }

    private static Specification<Booking> equal(String attribute, Object value) {
        return (root, query, cb) -> cb.equal(root.get(attribute), value);
    }

    /** Stay period still running on {@code from}: exclusive end_date is after it. */
    private static Specification<Booking> endsAfter(LocalDate from) {
        return (root, query, cb) -> cb.greaterThan(root.get("endDate"), from);
    }

    private static Specification<Booking> startsOnOrBefore(LocalDate to) {
        return (root, query, cb) -> cb.lessThan(root.get("startDate"), LeasingDates.toEndExclusive(to));
    }
}
