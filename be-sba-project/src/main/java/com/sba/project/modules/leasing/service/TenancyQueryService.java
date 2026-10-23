package com.sba.project.modules.leasing.service;

import java.util.Optional;
import java.util.UUID;

/**
 * Read API that leasing publishes for other modules (maintenance, billing, Zalo link). A tenancy is an
 * ACTIVE ContractTenant of an ACTIVE main Contract; a TENANT role alone is not a tenancy.
 */
public interface TenancyQueryService {
    /** Current tenancy of the user in the Room, if any (most recent start first). */
    Optional<UUID> findActiveContractTenantId(UUID userId, UUID roomId);

    /** P1-121: whether the user currently rents in the Branch. */
    boolean hasActiveTenancyInBranch(UUID userId, UUID branchId);
}
