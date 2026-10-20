package com.sba.project.common.security;

import com.sba.project.common.exceptions.AppException;
import com.sba.project.common.exceptions.ErrorCode;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** TEMPORARY fail-closed placeholder until Student 1's JWT principal is merged: nobody is logged in. */
@Component
public class PendingSecurityCurrentUserProvider implements CurrentUserProvider {
    @Override
    public UUID currentUserId() {
        throw new AppException(ErrorCode.UNAUTHENTICATED);
    }
}
