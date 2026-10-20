package com.sba.project.modules.community.specifications;

import com.sba.project.modules.community.entity.Thread;
import com.sba.project.modules.community.enums.ThreadStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class ThreadSpecifications {
    private ThreadSpecifications() {
    }

    /** P3-01: the author's own threads, any status, optionally filtered. */
    public static Specification<Thread> ownedBy(UUID authorUserId, ThreadStatus status) {
        List<Specification<Thread>> filters = new ArrayList<>();
        filters.add((root, query, cb) -> cb.equal(root.get("authorUserId"), authorUserId));
        if (status != null) filters.add((root, query, cb) -> cb.equal(root.get("status"), status));
        return Specification.allOf(filters);
    }

    /** P3-03: what other logged-in users may browse. OPEN only; HIDDEN/CLOSED stay out of the browse list. */
    public static Specification<Thread> browsable(String keyword) {
        List<Specification<Thread>> filters = new ArrayList<>();
        filters.add((root, query, cb) -> cb.equal(root.get("status"), ThreadStatus.OPEN));
        if (keyword != null && !keyword.isBlank()) {
            String pattern = "%" + keyword.trim().toLowerCase(Locale.ROOT).replace("\\", "\\\\")
                    .replace("%", "\\%").replace("_", "\\_") + "%";
            filters.add((root, query, cb) -> cb.like(cb.lower(root.get("title")), pattern, '\\'));
        }
        return Specification.allOf(filters);
    }
}
