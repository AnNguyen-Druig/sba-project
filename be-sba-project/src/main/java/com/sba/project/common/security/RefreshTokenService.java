package com.sba.project.common.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenService {
    private final long expirationMillis;
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenService(@Value("${app.jwt.refresh-expiration-ms:604800000}") long expirationMillis) {
        if (expirationMillis < 1_000) {
            throw new IllegalStateException("app.jwt.refresh-expiration-ms must be at least 1000");
        }
        this.expirationMillis = expirationMillis;
    }

    public IssuedToken issue(UUID userId) {
        byte[] secret = new byte[32];
        secureRandom.nextBytes(secret);
        String token = userId + "." + Base64.getUrlEncoder().withoutPadding().encodeToString(secret);
        return new IssuedToken(token, hash(token), Instant.now().plusMillis(expirationMillis));
    }

    public UUID getUserId(String token) {
        if (token == null) return null;
        int separator = token.indexOf('.');
        if (separator <= 0 || separator != token.lastIndexOf('.')) return null;
        try {
            UUID userId = UUID.fromString(token.substring(0, separator));
            byte[] secret = Base64.getUrlDecoder().decode(token.substring(separator + 1));
            return secret.length == 32 ? userId : null;
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    public String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    public boolean matches(String storedHash, String token) {
        if (storedHash == null || !storedHash.matches("[0-9a-f]{64}") || token == null) return false;
        return MessageDigest.isEqual(
                storedHash.getBytes(StandardCharsets.US_ASCII),
                hash(token).getBytes(StandardCharsets.US_ASCII));
    }

    public long getExpirationSeconds() {
        return expirationMillis / 1000;
    }

    public record IssuedToken(String value, String hash, Instant expiresAt) {
    }
}
