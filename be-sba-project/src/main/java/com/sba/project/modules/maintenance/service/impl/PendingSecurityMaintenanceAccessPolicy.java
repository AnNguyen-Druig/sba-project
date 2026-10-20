package com.sba.project.modules.maintenance.service.impl;

import com.sba.project.common.exceptions.AppException;
import com.sba.project.common.exceptions.ErrorCode;
import com.sba.project.modules.maintenance.service.MaintenanceAccessPolicy;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * TEMPORARY fail-closed placeholder until Student 1's JWT principal and Branch-scope check are
 * merged. It grants nothing: every maintenance call is rejected as unauthenticated. Replace this
 * class with an adapter over the shared security utilities; do not add permissive logic here.
 */
@Component
public class PendingSecurityMaintenanceAccessPolicy implements MaintenanceAccessPolicy {
    @Override
    public UUID currentUserId() {
        throw new AppException(ErrorCode.UNAUTHENTICATED);
    }

    @Override
    public boolean canManageRoom(UUID userId, UUID roomId) {
        return false;
    }

    @Override
    public boolean canManageBranch(UUID userId, UUID branchId) {
        return false;
    }
}
