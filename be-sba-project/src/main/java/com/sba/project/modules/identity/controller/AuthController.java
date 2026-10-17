package com.sba.project.modules.identity.controller;

import com.sba.project.common.base.ApiResponse;
import com.sba.project.common.security.AuthPrincipal;
import com.sba.project.modules.identity.dto.request.EmailVerificationRequest;
import com.sba.project.modules.identity.dto.request.LoginRequest;
import com.sba.project.modules.identity.dto.request.RegisterRequest;
import com.sba.project.modules.identity.dto.response.AuthResponse;
import com.sba.project.modules.identity.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.Duration;
import java.time.Instant;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final String REFRESH_COOKIE_NAME = "refresh_token";

    private final AuthService authService;
    private final boolean refreshCookieSecure;
    private final String refreshCookieSameSite;

    public AuthController(
            AuthService authService,
            @Value("${app.jwt.refresh-cookie-secure:false}") boolean refreshCookieSecure,
            @Value("${app.jwt.refresh-cookie-same-site:Strict}") String refreshCookieSameSite) {
        this.authService = authService;
        this.refreshCookieSecure = refreshCookieSecure;
        this.refreshCookieSameSite = refreshCookieSameSite;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        ApiResponse<Void> response = ApiResponse.success(
                "Account created. Check your email to verify it before signing in.",
                null);
        response.setStatus(HttpStatus.ACCEPTED.value());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@Valid @RequestBody EmailVerificationRequest request) {
        authService.verifyEmail(request.token());
        return ResponseEntity.ok(ApiResponse.success("Email verified successfully. You can now sign in.", null));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse session = authService.login(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie(session).toString())
                .body(ApiResponse.success("Login successful", session));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @CookieValue(name = REFRESH_COOKIE_NAME, required = false) String refreshToken) {
        AuthResponse session = authService.refresh(refreshToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie(session).toString())
                .body(ApiResponse.success("Token refreshed successfully", session));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal AuthPrincipal principal) {
        authService.logout(principal);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearRefreshCookie().toString())
                .body(ApiResponse.success("Logout successful", null));
    }

    private org.springframework.http.ResponseCookie refreshCookie(AuthResponse session) {
        long maxAge = Math.max(0, Duration.between(Instant.now(), session.refreshTokenExpiresAt()).getSeconds());
        return org.springframework.http.ResponseCookie.from(REFRESH_COOKIE_NAME, session.refreshToken())
                .httpOnly(true)
                .secure(refreshCookieSecure)
                .sameSite(refreshCookieSameSite)
                .path("/api/auth")
                .maxAge(maxAge)
                .build();
    }

    private org.springframework.http.ResponseCookie clearRefreshCookie() {
        return org.springframework.http.ResponseCookie.from(REFRESH_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(refreshCookieSecure)
                .sameSite(refreshCookieSameSite)
                .path("/api/auth")
                .maxAge(0)
                .build();
    }
}
