package com.sba.project.modules.leasing.repository;

import com.sba.project.modules.leasing.entity.ContractTenant;
import com.sba.project.modules.leasing.enums.ContractTenantStatus;
import java.time.Instant;
import java.util.Collection;
import java.util.UUID;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import tools.jackson.databind.JsonNode;

/**
 * Narrow write slice for P2-19: only the handover columns of contract_tenants (handover_at/by,
 * snapshot, evidence). Everything else on that table stays with Student 3's leasing services.
 */
public interface ContractTenantHandoverRepository extends Repository<ContractTenant, UUID> {
    /** Records the handover once; returns 0 when already recorded or the member is not in a valid state. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update ContractTenant ct set ct.handoverAt = :now, ct.handoverBy = :by, ct.handoverSnapshot = :snapshot,
                ct.handoverEvidenceRefs = :evidence, ct.version = ct.version + 1, ct.updatedAt = :now
            where ct.id = :id and ct.handoverAt is null and ct.status in :statuses
            """)
    int record(@Param("id") UUID id, @Param("by") UUID by, @Param("snapshot") JsonNode snapshot,
            @Param("evidence") JsonNode evidence, @Param("statuses") Collection<ContractTenantStatus> statuses,
            @Param("now") Instant now);
}
