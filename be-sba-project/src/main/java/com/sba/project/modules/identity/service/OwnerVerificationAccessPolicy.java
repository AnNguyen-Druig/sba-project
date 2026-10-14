package com.sba.project.modules.identity.service;

import java.util.UUID;

/**
 * Integration contract owned by Student 1 (authentication/roles). Returns the authenticated ADMIN's
 * user id; throws {@code AppException(UNAUTHENTICATED)} without a session and
 * {@code AppException(UNAUTHORIZED)} when the caller does not hold the ADMIN role (P1-08, P1-132).
 */
public interface OwnerVerificationAccessPolicy {
    UUID requireAdminId();
}
