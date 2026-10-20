package com.sba.project.modules.maintenance.service.impl;

import com.sba.project.modules.maintenance.service.RoomTenancyPort;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** TEMPORARY fail-closed placeholder until Student 3's tenancy lookup is merged: nobody is a tenant. */
@Component
public class PendingLeasingRoomTenancyPort implements RoomTenancyPort {
    @Override
    public Optional<UUID> findActiveContractTenantId(UUID userId, UUID roomId) {
        return Optional.empty();
    }
}
