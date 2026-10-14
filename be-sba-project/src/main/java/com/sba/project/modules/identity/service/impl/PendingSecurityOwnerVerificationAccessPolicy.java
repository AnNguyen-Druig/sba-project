package com.sba.project.modules.identity.service.impl;

import com.sba.project.common.exceptions.AppException;
import com.sba.project.common.exceptions.ErrorCode;
import com.sba.project.modules.identity.service.OwnerVerificationAccessPolicy;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** TEMPORARY fail-closed placeholder until Student 1's security is merged: no one is an ADMIN. */
@Component
public class PendingSecurityOwnerVerificationAccessPolicy implements OwnerVerificationAccessPolicy {
    @Override
    public UUID requireAdminId() {
        throw new AppException(ErrorCode.UNAUTHENTICATED);
    }
}
