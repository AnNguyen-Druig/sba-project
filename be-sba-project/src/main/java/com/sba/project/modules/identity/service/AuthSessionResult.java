package com.sba.project.modules.identity.service;

import com.sba.project.modules.identity.dto.response.AuthResponse;
import java.time.Instant;

public record AuthSessionResult(
        AuthResponse response,
        String refreshToken,
        Instant refreshTokenExpiresAt) {
}
