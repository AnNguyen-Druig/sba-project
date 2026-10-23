package com.sba.project.modules.property.repository;

import com.sba.project.modules.property.entity.BranchPolicy;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BranchPolicyRepository extends JpaRepository<BranchPolicy, UUID> {
    /** Policies are versioned and immutable; the effective one is the highest version already in force. */
    Optional<BranchPolicy> findFirstByBranchIdAndEffectiveFromLessThanEqualOrderByVersionDesc(UUID branchId,
            Instant at);
}
