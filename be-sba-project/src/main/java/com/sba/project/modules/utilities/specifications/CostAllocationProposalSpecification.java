package com.sba.project.modules.utilities.specifications;

import com.sba.project.modules.utilities.entity.CostAllocationProposal;
import com.sba.project.modules.utilities.enums.CostAllocationProposalStatus;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * JPA Specifications for dynamic search and filtering on {@link CostAllocationProposal}.
 */
public final class CostAllocationProposalSpecification {

    private CostAllocationProposalSpecification() {}

    public static Specification<CostAllocationProposal> withFilters(
            UUID roomServiceChargeId,
            CostAllocationProposalStatus status,
            UUID proposedBy) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (roomServiceChargeId != null) {
                predicates.add(cb.equal(root.get("roomServiceChargeId"), roomServiceChargeId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (proposedBy != null) {
                predicates.add(cb.equal(root.get("proposedBy"), proposedBy));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
