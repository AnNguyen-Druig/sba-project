package com.sba.project.modules.maintenance.service;

import java.util.UUID;

/**
 * Authorization port used by maintenance services. The real adapter belongs on top of
 * Student 1's security (authenticated principal + Branch scope, P1-10/P1-134) and Student 2's
 * Room -> Branch lookup; maintenance must not re-implement either.
 */
public interface MaintenanceAccessPolicy {
    /** Authenticated user id; throws {@code AppException(UNAUTHENTICATED)} when there is none. */
    UUID currentUserId();

    /** True when the user is the Owner of the Room's Branch or its assigned Manager (BR-03). */
    boolean canManageRoom(UUID userId, UUID roomId);

    /** Same rule at Branch level, for Branch-wide statistics. */
    boolean canManageBranch(UUID userId, UUID branchId);
}
