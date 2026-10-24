package com.sba.project.common.security;

import com.sba.project.common.exceptions.AppException;
import com.sba.project.common.exceptions.ErrorCode;
import com.sba.project.modules.identity.service.impl.SecurityOwnerVerificationAccessPolicy;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecurityCurrentUserProviderTests {
    private static final UUID USER = UUID.randomUUID();
    private final SecurityCurrentUserProvider provider = new SecurityCurrentUserProvider();
    private final SecurityOwnerVerificationAccessPolicy adminPolicy = new SecurityOwnerVerificationAccessPolicy(provider);

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    private static void login(String role) {
        AuthPrincipal principal = new AuthPrincipal(USER.toString(), "a@example.test", role, "jti", Instant.now());
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                principal, null, List.of(new SimpleGrantedAuthority("ROLE_" + role))));
    }

    private static void assertError(Runnable call, ErrorCode code) {
        assertThatThrownBy(call::run).isInstanceOf(AppException.class)
                .extracting(e -> ((AppException) e).getErrorCode()).isEqualTo(code);
    }

    @Test
    void readsTheJwtPrincipal() {
        login("TENANT");
        assertThat(provider.currentUserId()).isEqualTo(USER);
        assertThat(provider.hasRole("TENANT")).isTrue();
        assertThat(provider.hasRole("ADMIN")).isFalse();
    }

    @Test
    void anonymousCallerIsUnauthenticated() {
        assertError(provider::currentUserId, ErrorCode.UNAUTHENTICATED);
    }

    @Test
    void ownerVerificationRequiresAdminRole() {
        login("OWNER");
        assertError(adminPolicy::requireAdminId, ErrorCode.UNAUTHORIZED);
        login("ADMIN");
        assertThat(adminPolicy.requireAdminId()).isEqualTo(USER);
    }
}
