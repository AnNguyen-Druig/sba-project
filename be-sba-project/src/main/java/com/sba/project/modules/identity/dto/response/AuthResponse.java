package com.sba.project.modules.identity.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.Instant;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        String accountId,
        String email,
        String fullName,
        String role,
        @JsonIgnore String refreshToken,
        @JsonIgnore Instant refreshTokenExpiresAt) {
}
