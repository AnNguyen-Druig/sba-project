package com.sba.project.common.security;

import java.util.UUID;

/**
 * Port to the authenticated principal, owned by Student 1's security work. Services that only need
 * "who is calling" (no Branch scope) depend on this instead of the Spring Security context directly.
 */
public interface CurrentUserProvider {
    /** Authenticated user id; throws {@code AppException(UNAUTHENTICATED)} when there is none. */
    UUID currentUserId();
}
