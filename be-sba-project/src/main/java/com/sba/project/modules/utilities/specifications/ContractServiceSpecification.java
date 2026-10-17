package com.sba.project.modules.utilities.specifications;

import com.sba.project.modules.utilities.entity.ContractService;
import com.sba.project.modules.utilities.enums.ContractServiceStatus;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * JPA Specifications for dynamic search and filtering on {@link ContractService}.
 */
public final class ContractServiceSpecification {

    private ContractServiceSpecification() {}

    public static Specification<ContractService> withFilters(
            UUID contractId,
            UUID contractTenantId,
            UUID roomServiceId,
            ContractServiceStatus status) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (contractId != null) {
                predicates.add(cb.equal(root.get("contractId"), contractId));
            }
            if (contractTenantId != null) {
                predicates.add(cb.equal(root.get("contractTenantId"), contractTenantId));
            }
            if (roomServiceId != null) {
                predicates.add(cb.equal(root.get("roomServiceId"), roomServiceId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
