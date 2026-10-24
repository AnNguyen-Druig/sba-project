package com.sba.project.modules.utilities.util;

import com.sba.project.common.security.AuthPrincipal;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Helper utility to resolve the current authenticated user's UUID from {@link SecurityContextHolder}.
 * Security role validation and JWT filters are handled by Student 1 (Auth module).
 */
public final class SecurityUtils {

    private static final UUID DEFAULT_SYSTEM_USER = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private SecurityUtils() {}

    /**
     * Retrieves current authenticated user UUID, resolving from AuthPrincipal or auth.getName(),
     * falling back to a dummy UUID if unauthenticated (e.g. tests).
     */
    public static UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            if (auth.getPrincipal() instanceof AuthPrincipal principal && principal.accountId() != null) {
                try {
                    return UUID.fromString(principal.accountId());
                } catch (IllegalArgumentException ignored) {
                }
            }
            if (auth.getName() != null) {
                try {
                    return UUID.fromString(auth.getName());
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        return DEFAULT_SYSTEM_USER;
    }
}
