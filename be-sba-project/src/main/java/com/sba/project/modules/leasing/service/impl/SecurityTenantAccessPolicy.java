package com.sba.project.modules.leasing.service.impl;

import com.sba.project.common.security.BranchScope;
import com.sba.project.common.security.CurrentUserProvider;
import com.sba.project.modules.leasing.service.TenantAccessPolicy;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Adapter over Student 1's JWT principal and the shared Owner/Manager Branch scope (P1-134). */
@Component
@RequiredArgsConstructor
public class SecurityTenantAccessPolicy implements TenantAccessPolicy {
    private final CurrentUserProvider currentUser;
    private final BranchScope branchScope;

    @Override
    public UUID currentUserId() {
        return currentUser.currentUserId();
    }

    @Override
    public boolean canManageBranch(UUID userId, UUID branchId) {
        return branchScope.canManageBranch(userId, branchId);
    }

    @Override
    public boolean canManageRoom(UUID userId, UUID roomId) {
        return branchScope.canManageRoom(userId, roomId);
    }
}
