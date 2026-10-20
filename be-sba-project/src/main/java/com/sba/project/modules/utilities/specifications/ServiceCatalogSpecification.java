package com.sba.project.modules.utilities.specifications;

import com.sba.project.modules.utilities.entity.ServiceCatalog;
import com.sba.project.modules.utilities.enums.BillingMethod;
import com.sba.project.modules.utilities.enums.ServiceType;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * JPA Specifications for dynamic search and filtering on {@link ServiceCatalog}.
 */
public final class ServiceCatalogSpecification {

    private ServiceCatalogSpecification() {}

    public static Specification<ServiceCatalog> withFilters(
            UUID branchId,
            ServiceType serviceType,
            BillingMethod billingMethod,
            Boolean mandatory,
            Boolean active,
            String name) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (branchId != null) {
                predicates.add(cb.equal(root.get("branchId"), branchId));
            }
            if (serviceType != null) {
                predicates.add(cb.equal(root.get("serviceType"), serviceType));
            }
            if (billingMethod != null) {
                predicates.add(cb.equal(root.get("billingMethod"), billingMethod));
            }
            if (mandatory != null) {
                predicates.add(cb.equal(root.get("mandatory"), mandatory));
            }
            if (active != null) {
                predicates.add(cb.equal(root.get("active"), active));
            }
            if (name != null && !name.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.trim().toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
