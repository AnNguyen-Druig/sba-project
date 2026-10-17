package com.sba.project.modules.identity.dto.response;

import com.sba.project.modules.identity.enums.UserStatus;
import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String phone,
        String fullName,
        UserStatus status,
        Instant createdAt) {
}
