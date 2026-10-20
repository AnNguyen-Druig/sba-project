package com.sba.project.modules.identity.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sba.project.common.security.AuthPrincipal;
import com.sba.project.modules.identity.dto.request.EmailVerificationRequest;
import com.sba.project.modules.identity.dto.request.LoginRequest;
import com.sba.project.modules.identity.dto.request.RegisterRequest;
import com.sba.project.modules.identity.dto.response.AuthResponse;
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
                "admin@example.com", "Admin", "ADMIN",
                "opaque-refresh-secret", Instant.now().plusSeconds(604800));
        when(authService.login(org.mockito.ArgumentMatchers.any(LoginRequest.class))).thenReturn(response);

        var result = controller.login(new LoginRequest());

        String cookie = result.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertThat(cookie).contains("refresh_token=opaque-refresh-secret", "HttpOnly", "SameSite=Strict", "Path=/api/auth");
        assertThat(cookie).doesNotContain("Secure");
        assertThat(result.getBody().getData()).isEqualTo(response);
        String json = tools.jackson.databind.json.JsonMapper.builder().build().writeValueAsString(result.getBody());
        assertThat(json).contains("accessToken").doesNotContain("opaque-refresh-secret", "refreshTokenExpiresAt");
    }

    @Test
    void registerAcceptsAccountAndDoesNotCreateLoginSession() {
        var result = controller.register(new RegisterRequest());

        verify(authService).register(org.mockito.ArgumentMatchers.any(RegisterRequest.class));
        assertThat(result.getStatusCode().value()).isEqualTo(202);
        assertThat(result.getHeaders().getFirst(HttpHeaders.SET_COOKIE)).isNull();
        assertThat(result.getBody().getMessage()).contains("Check your email");
    }

    @Test
    void verifyEmailReturnsSuccessAfterServiceConfirmsToken() {
        var result = controller.verifyEmail(new EmailVerificationRequest("verification-token"));

        verify(authService).verifyEmail("verification-token");
        assertThat(result.getBody().getMessage()).contains("Email verified");
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
