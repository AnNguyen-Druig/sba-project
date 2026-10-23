package com.sba.project.modules.maintenance.service.impl;

import com.sba.project.modules.leasing.service.TenancyQueryService;
import com.sba.project.modules.maintenance.service.RoomTenancyPort;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Maintenance's tenancy check, answered by leasing's public read API (Student 3). */
@Component
@RequiredArgsConstructor
public class LeasingRoomTenancyAdapter implements RoomTenancyPort {
    private final TenancyQueryService tenancyQueryService;

    @Override
    public Optional<UUID> findActiveContractTenantId(UUID userId, UUID roomId) {
        return tenancyQueryService.findActiveContractTenantId(userId, roomId);
    }
}
