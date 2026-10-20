package com.sba.project.common.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.sba.project.modules.identity.entity.User;
import com.sba.project.modules.identity.enums.RoleCode;
import io.jsonwebtoken.JwtException;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtTokenServiceTests {
    private JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() {
        jwtTokenService = new JwtTokenService("test-only-secret-that-is-at-least-thirty-two-bytes", 3_600_000);
        jwtTokenService.initialize();
    }

    @Test
    void issuedTokenRoundTripsIdentityAndRole() {
        UUID accountId = UUID.randomUUID();
        User user = mock(User.class);
        when(user.getId()).thenReturn(accountId);
        when(user.getEmail()).thenReturn("user@example.com");

        JwtTokenService.IssuedToken issued = jwtTokenService.issue(user, RoleCode.USER);
        AuthPrincipal principal = jwtTokenService.parse(issued.value());

        assertThat(principal.accountId()).isEqualTo(accountId.toString());
        assertThat(principal.email()).isEqualTo("user@example.com");
        assertThat(principal.role()).isEqualTo("USER");
        assertThat(principal.tokenId()).isNotBlank();
        assertThat(principal.expiresAt()).isEqualTo(issued.expiresAt());
    }

    @Test
    void rejectsTokenWithModifiedSignature() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(UUID.randomUUID());
        when(user.getEmail()).thenReturn("user@example.com");
        String token = jwtTokenService.issue(user, RoleCode.USER).value();
        char replacement = token.charAt(token.length() - 1) == 'A' ? 'B' : 'A';
        String modifiedToken = token.substring(0, token.length() - 1) + replacement;

        assertThatThrownBy(() -> jwtTokenService.parse(modifiedToken))
                .isInstanceOf(JwtException.class);
    }
}
