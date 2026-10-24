package com.sba.project.modules.identity.service.impl;

import com.sba.project.common.exceptions.AppException;
import com.sba.project.common.exceptions.ErrorCode;
import com.sba.project.common.security.SecurityCurrentUserProvider;
import com.sba.project.modules.identity.service.OwnerVerificationAccessPolicy;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** ADMIN check over Student 1's JWT principal (P1-08, P1-132). */
@Component
@RequiredArgsConstructor
public class SecurityOwnerVerificationAccessPolicy implements OwnerVerificationAccessPolicy {
    private final SecurityCurrentUserProvider currentUser;

    @Override
    public UUID requireAdminId() {
        UUID userId = currentUser.currentUserId();
        if (!currentUser.hasRole("ADMIN")) throw new AppException(ErrorCode.UNAUTHORIZED);
        return userId;
    }
}
