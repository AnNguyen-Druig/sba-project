package com.sba.project.modules.utilities.specifications;

import com.sba.project.modules.utilities.entity.RoomServiceCharge;
import com.sba.project.modules.utilities.enums.RoomServiceChargeStatus;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * JPA Specifications for dynamic search and filtering on {@link RoomServiceCharge}.
 */
public final class RoomServiceChargeSpecification {

    private RoomServiceChargeSpecification() {}

    public static Specification<RoomServiceCharge> withFilters(
            UUID roomId,
            UUID roomServiceId,
            LocalDate billingMonth,
            RoomServiceChargeStatus status,
            LocalDate startDateFrom,
            LocalDate endDateTo) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (roomId != null) {
                predicates.add(cb.equal(root.get("roomId"), roomId));
            }
            if (roomServiceId != null) {
                predicates.add(cb.equal(root.get("roomServiceId"), roomServiceId));
            }
            if (billingMonth != null) {
                predicates.add(cb.equal(root.get("billingMonth"), billingMonth));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (startDateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startDate"), startDateFrom));
            }
            if (endDateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("endDate"), endDateTo));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
