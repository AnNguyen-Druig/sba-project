package com.sba.project.modules.maintenance.repository;

import com.sba.project.modules.maintenance.entity.ReportRequest;
import com.sba.project.modules.maintenance.enums.ReportRequestStatus;
import java.time.Instant;
import java.util.Collection;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * No @Version on the entity, so every write that must not be lost is a compare-and-set on
 * (status, version) that returns the number of updated rows (0 = concurrent change).
 */
public interface ReportRequestRepository extends JpaRepository<ReportRequest, UUID>, JpaSpecificationExecutor<ReportRequest> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update ReportRequest r set r.status = :to, r.version = r.version + 1, r.updatedAt = :now
            where r.id = :id and r.status = :from and r.version = :version
            """)
    int transition(@Param("id") UUID id, @Param("from") ReportRequestStatus from, @Param("to") ReportRequestStatus to,
            @Param("version") long version, @Param("now") Instant now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update ReportRequest r set r.status = :to, r.completedAt = :now, r.version = r.version + 1, r.updatedAt = :now
            where r.id = :id and r.status = :from and r.version = :version
            """)
    int complete(@Param("id") UUID id, @Param("from") ReportRequestStatus from, @Param("to") ReportRequestStatus to,
            @Param("version") long version, @Param("now") Instant now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update ReportRequest r set r.assignedTo = :assignee, r.version = r.version + 1, r.updatedAt = :now
            where r.id = :id and r.version = :version and r.status in :openStatuses
            """)
    int assign(@Param("id") UUID id, @Param("assignee") UUID assignee, @Param("version") long version,
            @Param("openStatuses") Collection<ReportRequestStatus> openStatuses, @Param("now") Instant now);

    /** P2-31: one rating per COMPLETED request, only by its requester. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update ReportRequest r set r.rating = :rating, r.ratingComment = :comment, r.ratedAt = :now,
                r.version = r.version + 1, r.updatedAt = :now
            where r.id = :id and r.requesterUserId = :requester and r.rating is null
              and r.status = com.sba.project.modules.maintenance.enums.ReportRequestStatus.COMPLETED
            """)
    int rate(@Param("id") UUID id, @Param("requester") UUID requester, @Param("rating") int rating,
            @Param("comment") String comment, @Param("now") Instant now);

    /** Atomic JSONB append: concurrent attachments cannot overwrite each other. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            update rental.report_requests
            set evidence_refs = evidence_refs || cast(:reference as jsonb), version = version + 1, updated_at = now()
            where id = :id
            """, nativeQuery = true)
    int appendEvidence(@Param("id") UUID id, @Param("reference") String referenceJson);
}
