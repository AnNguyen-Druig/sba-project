package com.sba.project.modules.leasing.service;

import java.util.UUID;

/**
 * Authorization port over Student 1's security (principal + Owner/Manager Branch scope, P1-134).
 * Tenant lookups must not re-implement it.
 */
public interface TenantAccessPolicy {
    /** Authenticated user id; throws {@code AppException(UNAUTHENTICATED)} when there is none. */
    UUID currentUserId();

    boolean canManageBranch(UUID userId, UUID branchId);

    boolean canManageRoom(UUID userId, UUID roomId);
}
