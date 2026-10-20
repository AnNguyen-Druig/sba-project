package com.sba.project.modules.maintenance.specifications;

import com.sba.project.modules.maintenance.entity.ReportRequest;
import com.sba.project.modules.maintenance.enums.ReportRequestPriority;
import com.sba.project.modules.maintenance.enums.ReportRequestStatus;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class ReportRequestSpecifications {
    private ReportRequestSpecifications() {
    }

    /** Manager view (P1-114): one room, optional status/priority. */
    public static Specification<ReportRequest> forRoom(UUID roomId, ReportRequestStatus status,
            ReportRequestPriority priority, boolean overdueOnly, Instant now) {
        List<Specification<ReportRequest>> filters = new ArrayList<>();
        filters.add((root, query, cb) -> cb.equal(root.get("roomId"), roomId));
        if (status != null) filters.add((root, query, cb) -> cb.equal(root.get("status"), status));
        if (priority != null) filters.add((root, query, cb) -> cb.equal(root.get("priority"), priority));
        if (overdueOnly) filters.add(overdue(now));
        return Specification.allOf(filters);
    }

    /** P2-30 backlog: unfinished requests whose SLA deadline has passed. */
    public static Specification<ReportRequest> overdue(Instant now) {
        return (root, query, cb) -> cb.and(
                root.get("status").in(ReportRequestStatus.OPEN, ReportRequestStatus.ACCEPTED,
                        ReportRequestStatus.IN_PROGRESS),
                cb.isNotNull(root.get("slaDueAt")), cb.lessThan(root.<Instant>get("slaDueAt"), now));
    }

    /** Tenant view (P1-119): only the requester's own requests. */
    public static Specification<ReportRequest> forRequester(UUID requesterUserId, ReportRequestStatus status) {
        List<Specification<ReportRequest>> filters = new ArrayList<>();
        filters.add((root, query, cb) -> cb.equal(root.get("requesterUserId"), requesterUserId));
        if (status != null) filters.add((root, query, cb) -> cb.equal(root.get("status"), status));
        return Specification.allOf(filters);
    }
}
