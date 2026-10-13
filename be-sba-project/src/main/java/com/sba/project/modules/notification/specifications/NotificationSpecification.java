package com.sba.project.modules.notification.specifications;

import com.sba.project.modules.notification.entity.Notification;
import com.sba.project.modules.notification.enums.NotificationChannel;
import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * JPA Specifications for dynamic filtering of Notification entities.
 * Implements RULE 06.
 */
public final class NotificationSpecification {

    private NotificationSpecification() {
        // Utility class
    }

    public static Specification<Notification> hasRecipientUserId(UUID recipientUserId) {
        return (root, query, cb) -> recipientUserId == null ? null : cb.equal(root.get("recipientUserId"), recipientUserId);
    }

    public static Specification<Notification> hasType(String type) {
        return (root, query, cb) -> (type == null || type.isBlank()) ? null : cb.equal(root.get("type"), type);
    }

    public static Specification<Notification> hasReferenceType(String referenceType) {
        return (root, query, cb) -> (referenceType == null || referenceType.isBlank()) ? null : cb.equal(root.get("referenceType"), referenceType);
    }

    public static Specification<Notification> hasReferenceId(UUID referenceId) {
        return (root, query, cb) -> referenceId == null ? null : cb.equal(root.get("referenceId"), referenceId);
    }

    public static Specification<Notification> hasChannel(NotificationChannel channel) {
        return (root, query, cb) -> channel == null ? null : cb.equal(root.get("channel"), channel);
    }

    public static Specification<Notification> isRead(Boolean isRead) {
        return (root, query, cb) -> {
            if (isRead == null) {
                return null;
            }
            return isRead ? cb.isNotNull(root.get("readAt")) : cb.isNull(root.get("readAt"));
        };
    }

    public static Specification<Notification> createdAtBetween(Instant from, Instant to) {
        return (root, query, cb) -> {
            if (from == null && to == null) {
                return null;
            }
            if (from != null && to != null) {
                return cb.between(root.get("createdAt"), from, to);
            }
            if (from != null) {
                return cb.greaterThanOrEqualTo(root.get("createdAt"), from);
            }
            return cb.lessThanOrEqualTo(root.get("createdAt"), to);
        };
    }

    public static Specification<Notification> withFilters(
            UUID recipientUserId,
            String type,
            String referenceType,
            UUID referenceId,
            NotificationChannel channel,
            Boolean isRead,
            Instant from,
            Instant to) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (recipientUserId != null) {
                predicates.add(cb.equal(root.get("recipientUserId"), recipientUserId));
            }
            if (type != null && !type.isBlank()) {
                predicates.add(cb.equal(root.get("type"), type));
            }
            if (referenceType != null && !referenceType.isBlank()) {
                predicates.add(cb.equal(root.get("referenceType"), referenceType));
            }
            if (referenceId != null) {
                predicates.add(cb.equal(root.get("referenceId"), referenceId));
            }
            if (channel != null) {
                predicates.add(cb.equal(root.get("channel"), channel));
            }
            if (isRead != null) {
                if (isRead) {
                    predicates.add(cb.isNotNull(root.get("readAt")));
                } else {
                    predicates.add(cb.isNull(root.get("readAt")));
                }
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), to));
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
