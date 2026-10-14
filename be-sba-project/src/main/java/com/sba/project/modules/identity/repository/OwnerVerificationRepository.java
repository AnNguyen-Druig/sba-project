package com.sba.project.modules.identity.repository;

import com.sba.project.modules.identity.entity.User;
import com.sba.project.modules.identity.enums.OwnerVerificationStatus;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Owner-verification review queries only (BE-06.1); account/auth queries belong to Student 1. */
public interface OwnerVerificationRepository extends JpaRepository<User, UUID> {
    Page<User> findByOwnerVerificationStatus(OwnerVerificationStatus status, Pageable pageable);

    /** Compare-and-set PENDING -> APPROVED/REJECTED; the database trigger also requires an ADMIN actor. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update User u set u.ownerVerificationStatus = :decision, u.ownerReviewedBy = :admin,
                u.ownerReviewedAt = :now, u.ownerReviewReason = :reason, u.updatedAt = :now
            where u.id = :id
              and u.ownerVerificationStatus = com.sba.project.modules.identity.enums.OwnerVerificationStatus.PENDING
            """)
    int decide(@Param("id") UUID id, @Param("decision") OwnerVerificationStatus decision,
            @Param("admin") UUID admin, @Param("reason") String reason, @Param("now") Instant now);
}
