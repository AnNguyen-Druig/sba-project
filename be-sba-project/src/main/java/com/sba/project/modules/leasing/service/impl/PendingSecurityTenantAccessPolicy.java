package com.sba.project.modules.leasing.service.impl;

import com.sba.project.common.exceptions.AppException;
import com.sba.project.common.exceptions.ErrorCode;
import com.sba.project.modules.leasing.service.TenantAccessPolicy;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** TEMPORARY fail-closed placeholder until Student 1's security is merged. */
@Component
public class PendingSecurityTenantAccessPolicy implements TenantAccessPolicy {
    @Override
    public UUID currentUserId() {
        throw new AppException(ErrorCode.UNAUTHENTICATED);
    }

    @Override
    public boolean canManageBranch(UUID userId, UUID branchId) {
        return false;
    }

    @Override
    public boolean canManageRoom(UUID userId, UUID roomId) {
        return false;
    }
}
