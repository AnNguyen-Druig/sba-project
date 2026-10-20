package com.sba.project.modules.maintenance.service;

import java.util.Optional;
import java.util.UUID;

/**
 * Integration contract owned by Student 3 (leasing: Contract/ContractTenant). Maintenance only needs
 * to know whether a user currently rents a room; it must not duplicate contract validation.
 */
public interface RoomTenancyPort {
    /** ContractTenant id of the user's ACTIVE tenancy (ACTIVE member of an ACTIVE contract) in the room. */
    Optional<UUID> findActiveContractTenantId(UUID userId, UUID roomId);
}
