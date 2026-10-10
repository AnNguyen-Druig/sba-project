package com.sba.project.security;

import com.sba.project.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String ACCESS_TOKEN = "access";
    private static final String REFRESH_TOKEN = "refresh";

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationInMs;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long jwtRefreshExpirationInMs;

    private Key key;

    @PostConstruct
    public void init() {
        key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Authentication authentication) {
        UserDetails principal = (UserDetails) authentication.getPrincipal();
        String role = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring("ROLE_".length()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Authenticated user has no role"));
        return generateToken(principal.getUsername(), role,
                ACCESS_TOKEN, jwtExpirationInMs);
    }

    public String generateAccessToken(User user) {
        return generateToken(user.getEmail(), user.getRole().name(), ACCESS_TOKEN, jwtExpirationInMs);
    }

    public String generateRefreshToken(User user) {
        return generateToken(user.getEmail(), user.getRole().name(), REFRESH_TOKEN, jwtRefreshExpirationInMs);
    }

    private String generateToken(String username, String role, String tokenType, long expirationMs) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(username)
                .claim("role", role)
                .claim(TOKEN_TYPE_CLAIM, tokenType)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUsernameFromJWT(String token) {
        return getClaimsFromJWT(token).getSubject();
    }

    public String getRoleFromJWT(String token) {
        return getClaimsFromJWT(token).get("role", String.class);
    }

    public String getTokenIdFromJWT(String token) {
        return getClaimsFromJWT(token).getId();
    }

    public Date getExpirationFromJWT(String token) {
        return getClaimsFromJWT(token).getExpiration();
    }

    public boolean isAccessToken(String token) {
        return ACCESS_TOKEN.equals(getClaimsFromJWT(token).get(TOKEN_TYPE_CLAIM, String.class));
    }

    public boolean isRefreshToken(String token) {
        return REFRESH_TOKEN.equals(getClaimsFromJWT(token).get(TOKEN_TYPE_CLAIM, String.class));
    }

    public Claims getClaimsFromJWT(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(String authToken) {
        try {
            getClaimsFromJWT(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    public long getAccessExpirationMs() {
        return jwtExpirationInMs;
    }
}
