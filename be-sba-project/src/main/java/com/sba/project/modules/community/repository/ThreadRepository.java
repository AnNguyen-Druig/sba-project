package com.sba.project.modules.community.repository;

import com.sba.project.modules.community.entity.Thread;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tools.jackson.databind.JsonNode;

/**
 * Writes are conditional on status OPEN (compare-and-set): a full-row save could overwrite an ADMIN
 * moderation (HIDDEN) made concurrently, and the database only lets an ADMIN change that state.
 */
public interface ThreadRepository extends JpaRepository<Thread, UUID>, JpaSpecificationExecutor<Thread> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Thread t set t.title = :title, t.description = :description, t.criteria = :criteria,
                t.updatedAt = :now
            where t.id = :id and t.status = com.sba.project.modules.community.enums.ThreadStatus.OPEN
            """)
    int updateOpen(@Param("id") UUID id, @Param("title") String title, @Param("description") String description,
            @Param("criteria") JsonNode criteria, @Param("now") Instant now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Thread t set t.status = com.sba.project.modules.community.enums.ThreadStatus.CLOSED,
                t.closedAt = :now, t.updatedAt = :now
            where t.id = :id and t.status = com.sba.project.modules.community.enums.ThreadStatus.OPEN
            """)
    int closeOpen(@Param("id") UUID id, @Param("now") Instant now);

    /** P3-14: expiry-based auto close; only threads that actually carry an expiry. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Thread t set t.status = com.sba.project.modules.community.enums.ThreadStatus.CLOSED,
                t.closedAt = :now, t.updatedAt = :now
            where t.status = com.sba.project.modules.community.enums.ThreadStatus.OPEN
              and t.expiresAt is not null and t.expiresAt <= :now
            """)
    int closeExpired(@Param("now") Instant now);
}
