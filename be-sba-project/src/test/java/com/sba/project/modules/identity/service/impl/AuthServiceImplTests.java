package com.sba.project.modules.identity.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sba.project.common.exceptions.AppException;
import com.sba.project.common.exceptions.ErrorCode;
import com.sba.project.common.security.JwtTokenService;
import com.sba.project.common.security.RefreshTokenService;
import com.sba.project.common.security.AuthPrincipal;
import com.sba.project.modules.identity.dto.request.LoginRequest;
import com.sba.project.modules.identity.dto.request.RegisterRequest;
import com.sba.project.modules.identity.entity.Role;
import com.sba.project.modules.identity.entity.User;
import com.sba.project.modules.identity.entity.UserRole;
import com.sba.project.modules.identity.enums.RoleCode;
import com.sba.project.modules.identity.enums.UserStatus;
import com.sba.project.modules.identity.repository.InvalidatedTokenRepository;
import com.sba.project.modules.identity.repository.RoleRepository;
import com.sba.project.modules.identity.repository.UserRepository;
import com.sba.project.modules.identity.repository.UserRoleRepository;
import com.sba.project.modules.identity.service.EmailVerificationService;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class AuthServiceImplTests {
    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private UserRoleRepository userRoleRepository;
    private InvalidatedTokenRepository invalidatedTokenRepository;
    private JwtTokenService jwtTokenService;
    private RefreshTokenService refreshTokenService;
    private BCryptPasswordEncoder passwordEncoder;
    private EmailVerificationService emailVerificationService;
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        roleRepository = mock(RoleRepository.class);
        userRoleRepository = mock(UserRoleRepository.class);
        invalidatedTokenRepository = mock(InvalidatedTokenRepository.class);
        jwtTokenService = mock(JwtTokenService.class);
        refreshTokenService = mock(RefreshTokenService.class);
        passwordEncoder = new BCryptPasswordEncoder();
        emailVerificationService = mock(EmailVerificationService.class);
        authService = new AuthServiceImpl(
                userRepository, roleRepository, userRoleRepository,
                invalidatedTokenRepository, jwtTokenService, refreshTokenService, passwordEncoder,
                emailVerificationService);
    }

    @Test
    void registerCreatesUnverifiedAccountAndSendsVerificationEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("user@example.com");
        request.setPassword("safe-password");
        request.setFullName("Test User");
        UUID userId = UUID.randomUUID();
        User user = mock(User.class);
        Role role = mock(Role.class);
        when(userRepository.existsByEmailIgnoreCase("user@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(user.getId()).thenReturn(userId);
        when(user.getEmail()).thenReturn("user@example.com");
        when(user.getFullName()).thenReturn("Test User");
        when(roleRepository.findByCode(RoleCode.USER)).thenReturn(Optional.of(role));
        when(role.getId()).thenReturn(UUID.randomUUID());
        authService.register(request);

        var savedUser = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(savedUser.capture());
        assertThat(savedUser.getValue().getEmail()).isEqualTo("user@example.com");
        assertThat(savedUser.getValue().getPasswordHash()).isNotEqualTo("safe-password");
        assertThat(passwordEncoder.matches("safe-password", savedUser.getValue().getPasswordHash())).isTrue();
        assertThat(savedUser.getValue().getStatus()).isEqualTo(UserStatus.PENDING_VERIFICATION);
        assertThat(savedUser.getValue().getEmailVerifiedAt()).isNull();
        verify(userRoleRepository).save(any());
        verify(emailVerificationService).sendVerificationEmail(user);
    }

    @Test
    void refreshRotatesRefreshTokenOnlyWhenCurrentTokenMatches() {
        String currentToken = UUID.randomUUID() + "." + "a".repeat(43);
        UUID userId = UUID.randomUUID();
        User user = mock(User.class);
        Role role = mock(Role.class);
        UserRole userRole = mock(UserRole.class);
        when(refreshTokenService.getUserId(currentToken)).thenReturn(userId);
        when(userRepository.findByIdForUpdate(userId)).thenReturn(Optional.of(user));
        when(user.getStatus()).thenReturn(UserStatus.ACTIVE);
        when(user.getRefreshTokenExpiresAt()).thenReturn(Instant.now().plusSeconds(300));
        when(user.getRefreshTokenHash()).thenReturn("b".repeat(64));
        when(refreshTokenService.matches("b".repeat(64), currentToken)).thenReturn(true);
        when(user.getId()).thenReturn(userId);
        when(user.getEmail()).thenReturn("user@example.com");
        when(user.getFullName()).thenReturn("Test User");
        when(userRoleRepository.findAllByIdUserId(userId)).thenReturn(java.util.List.of(userRole));
        when(userRole.getRoleReference()).thenReturn(role);
        when(role.getCode()).thenReturn(RoleCode.USER);
        when(jwtTokenService.issue(user, RoleCode.USER))
                .thenReturn(new JwtTokenService.IssuedToken("new-access-token", Instant.now().plusSeconds(3600)));
        when(jwtTokenService.getExpirationSeconds()).thenReturn(3600L);
        when(refreshTokenService.issue(userId)).thenReturn(new RefreshTokenService.IssuedToken(
                "rotated-refresh-token", "c".repeat(64), Instant.now().plusSeconds(604800)));

        var result = authService.refresh(currentToken);

        assertThat(result.accessToken()).isEqualTo("new-access-token");
        assertThat(result.refreshToken()).isEqualTo("rotated-refresh-token");
        verify(user).setRefreshTokenHash("c".repeat(64));
        verify(user).setRefreshTokenExpiresAt(org.mockito.ArgumentMatchers.any(Instant.class));
    }

    @Test
    void refreshRejectsExpiredOrMismatchedToken() {
        String presentedToken = UUID.randomUUID() + "." + "a".repeat(43);
        UUID userId = UUID.randomUUID();
        User user = mock(User.class);
        when(refreshTokenService.getUserId(presentedToken)).thenReturn(userId);
        when(userRepository.findByIdForUpdate(userId)).thenReturn(Optional.of(user));
        when(user.getStatus()).thenReturn(UserStatus.ACTIVE);
        when(user.getRefreshTokenExpiresAt()).thenReturn(Instant.now().minusSeconds(1));

        assertThatThrownBy(() -> authService.refresh(presentedToken))
                .isInstanceOf(AppException.class)
                .extracting(exception -> ((AppException) exception).getErrorCode())
                .isEqualTo(ErrorCode.UNAUTHENTICATED);
        verify(userRoleRepository, never()).findAllByIdUserId(any());
    }

    @Test
    void logoutRevokesAccessTokenAndClearsRefreshTokenForUser() {
        UUID userId = UUID.randomUUID();
        Instant expiresAt = Instant.now().plusSeconds(300);
        User user = mock(User.class);
        when(userRepository.findByIdForUpdate(userId)).thenReturn(Optional.of(user));

        authService.logout(new AuthPrincipal(
                userId.toString(), "user@example.com", "USER", "access-jti", expiresAt));

        verify(invalidatedTokenRepository).invalidate("access-jti", expiresAt);
        verify(userRepository).findByIdForUpdate(userId);
        verify(user).setRefreshTokenHash(null);
        verify(user).setRefreshTokenExpiresAt(null);
    }

    @Test
    void loginRejectsWrongPasswordWithoutIssuingToken() {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("wrong-password");
        User user = mock(User.class);
        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        when(user.getStatus()).thenReturn(UserStatus.ACTIVE);
        when(user.getPasswordHash()).thenReturn(passwordEncoder.encode("correct-password"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AppException.class)
                .extracting(exception -> ((AppException) exception).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_CREDENTIALS);
        verify(jwtTokenService, never()).issue(any(User.class), any(RoleCode.class));
    }

    @Test
    void loginRejectsCorrectPasswordUntilEmailIsVerified() {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("correct-password");
        User user = mock(User.class);
        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        when(user.getStatus()).thenReturn(UserStatus.PENDING_VERIFICATION);
        when(user.getPasswordHash()).thenReturn(passwordEncoder.encode("correct-password"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AppException.class)
                .extracting(exception -> ((AppException) exception).getErrorCode())
                .isEqualTo(ErrorCode.EMAIL_NOT_VERIFIED);
        verify(jwtTokenService, never()).issue(any(User.class), any(RoleCode.class));
    }
}
