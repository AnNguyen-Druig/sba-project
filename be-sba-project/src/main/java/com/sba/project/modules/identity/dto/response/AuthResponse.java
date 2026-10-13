package com.sba.project.modules.identity.dto.response;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        String accountId,
        String email,
        String fullName,
        String role) {
}
