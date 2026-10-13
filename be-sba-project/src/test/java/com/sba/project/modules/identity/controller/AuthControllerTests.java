package com.sba.project.modules.identity.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.sba.project.common.security.AuthPrincipal;
import com.sba.project.modules.identity.dto.request.LoginRequest;
import com.sba.project.modules.identity.dto.request.RegisterRequest;
import com.sba.project.modules.identity.dto.response.AuthResponse;
import com.sba.project.modules.identity.service.AuthSessionResult;
import com.sba.project.modules.identity.service.AuthService;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

class AuthControllerTests {
    private AuthService authService;
    private AuthController controller;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        controller = new AuthController(authService, false, "Strict");
    }

    @Test
    void loginReturnsRefreshTokenOnlyAsHttpOnlyCookie() {
        AuthResponse response = new AuthResponse(
                "access-token", "Bearer", 3600, UUID.randomUUID().toString(),
                "admin@example.com", "Admin", "ADMIN");
        AuthSessionResult session = new AuthSessionResult(
                response, "opaque-refresh-secret", Instant.now().plusSeconds(604800));
        when(authService.login(org.mockito.ArgumentMatchers.any(LoginRequest.class))).thenReturn(session);

        var result = controller.login(new LoginRequest());

        String cookie = result.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertThat(cookie).contains("refresh_token=opaque-refresh-secret", "HttpOnly", "SameSite=Strict", "Path=/api/auth");
        assertThat(cookie).doesNotContain("Secure");
        assertThat(result.getBody().getData()).isEqualTo(response);
    }

    @Test
    void logoutClearsRefreshCookie() {
        AuthPrincipal principal = new AuthPrincipal(
                UUID.randomUUID().toString(), "admin@example.com", "ADMIN", "jti", Instant.now().plusSeconds(100));

        var result = controller.logout(principal);

        String cookie = result.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertThat(cookie).contains("refresh_token=", "HttpOnly", "SameSite=Strict", "Path=/api/auth", "Max-Age=0");
    }
}
