package com.sba.project.common.security;

import com.sba.project.modules.identity.entity.User;
import com.sba.project.modules.identity.enums.RoleCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenService {
    private final String jwtSecret;
    private final long expirationMillis;
    private SecretKey signingKey;

    public JwtTokenService(
            @Value("${JWT_SECRET}") String jwtSecret,
            @Value("${app.jwt.expiration-ms:3600000}") long expirationMillis) {
        this.jwtSecret = jwtSecret;
        this.expirationMillis = expirationMillis;
    }

    @PostConstruct
    void initialize() {
        byte[] secretBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET must contain at least 32 UTF-8 bytes");
        }
        if (expirationMillis < 1_000) {
            throw new IllegalStateException("app.jwt.expiration-ms must be at least 1000");
        }
        signingKey = Keys.hmacShaKeyFor(secretBytes);
    }

    public IssuedToken issue(User user, RoleCode role) {
        Instant issuedAt = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Instant expiresAt = issuedAt.plusMillis(expirationMillis).truncatedTo(ChronoUnit.SECONDS);
        String token = Jwts.builder()
                .subject(user.getId().toString())
                .id(UUID.randomUUID().toString())
                .claim("email", user.getEmail())
                .claim("role", role.name())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();
        return new IssuedToken(token, expiresAt);
    }

    public AuthPrincipal parse(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        String email = claims.get("email", String.class);
        String role = claims.get("role", String.class);
        String tokenId = claims.getId();
        String accountId = claims.getSubject();
        if (accountId == null || accountId.isBlank()
                || email == null || role == null || tokenId == null || claims.getExpiration() == null) {
            throw new IllegalArgumentException("JWT is missing required claims");
        }
        return new AuthPrincipal(
                accountId,
                email,
                role,
                tokenId,
                claims.getExpiration().toInstant());
    }

    public long getExpirationSeconds() {
        return expirationMillis / 1000;
    }

    public record IssuedToken(String value, Instant expiresAt) {
    }
}
