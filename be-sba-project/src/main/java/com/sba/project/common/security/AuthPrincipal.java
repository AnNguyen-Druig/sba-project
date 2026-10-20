package com.sba.project.common.security;

import java.time.Instant;

public record AuthPrincipal(String accountId, String email, String role, String tokenId, Instant expiresAt) {
}
