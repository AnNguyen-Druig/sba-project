package com.sba.project.modules.identity.service.impl;

import com.sba.project.common.exceptions.AppException;
import com.sba.project.common.exceptions.ErrorCode;
import com.sba.project.common.security.AuthPrincipal;
import com.sba.project.common.security.JwtTokenService;
import com.sba.project.common.security.JwtTokenService.IssuedToken;
import com.sba.project.common.security.RefreshTokenService;
import com.sba.project.modules.identity.dto.request.LoginRequest;
import com.sba.project.modules.identity.dto.request.RegisterRequest;
import com.sba.project.modules.identity.dto.response.AuthResponse;
import com.sba.project.modules.identity.entity.Role;
import com.sba.project.modules.identity.entity.User;
import com.sba.project.modules.identity.entity.UserRole;
import com.sba.project.modules.identity.entity.UserRoleId;
import com.sba.project.modules.identity.enums.RoleCode;
import com.sba.project.modules.identity.enums.UserStatus;
import com.sba.project.modules.identity.repository.RoleRepository;
import com.sba.project.modules.identity.repository.InvalidatedTokenRepository;
import com.sba.project.modules.identity.repository.UserRepository;
import com.sba.project.modules.identity.repository.UserRoleRepository;
import com.sba.project.modules.identity.service.AuthService;
import com.sba.project.modules.identity.service.EmailVerificationService;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final InvalidatedTokenRepository invalidatedTokenRepository;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;

    public AuthServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            UserRoleRepository userRoleRepository,
            InvalidatedTokenRepository invalidatedTokenRepository,
            JwtTokenService jwtTokenService,
            RefreshTokenService refreshTokenService,
            PasswordEncoder passwordEncoder,
            EmailVerificationService emailVerificationService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.invalidatedTokenRepository = invalidatedTokenRepository;
        this.jwtTokenService = jwtTokenService;
        this.refreshTokenService = refreshTokenService;
        this.passwordEncoder = passwordEncoder;
        this.emailVerificationService = emailVerificationService;
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        String password = request.getPassword();
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new AppException(ErrorCode.PASSWORD_INVALID);
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        User user;
        RoleCode roleCode = RoleCode.USER;
        try {
            user = new User();
            user.setEmail(email);
            user.setPasswordHash(passwordEncoder.encode(password));
            user.setFullName(request.getFullName().trim());
            user.setStatus(UserStatus.PENDING_VERIFICATION);
            user = userRepository.save(user);

            Role role = roleRepository.findByCode(roleCode)
                    .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
            UserRole userRole = new UserRole();
            userRole.setId(new UserRoleId(user.getId(), role.getId()));
            userRoleRepository.save(userRole);
            emailVerificationService.sendVerificationEmail(user);
        } catch (DataIntegrityViolationException exception) {
            throw new AppException(ErrorCode.DATA_CONFLICT);
        }
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        emailVerificationService.verifyEmail(token);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.getEmail().trim())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDENTIALS));
        if (user.getPasswordHash() == null
                || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }
        if (user.getStatus() == UserStatus.PENDING_VERIFICATION) {
            throw new AppException(ErrorCode.EMAIL_NOT_VERIFIED);
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }
        List<RoleCode> roles = userRoleRepository.findAllByIdUserId(user.getId()).stream()
                .map(UserRole::getRoleReference)
                .filter(role -> role != null)
                .map(Role::getCode)
                .distinct()
                .sorted(Comparator.comparingInt(AuthServiceImpl::rolePriority))
                .toList();
        if (roles.isEmpty()) {
            throw new AppException(ErrorCode.USER_ACCOUNT_UNAVAILABLE);
        }
        return createAuthSession(user, roles.getFirst());
    }

    @Override
    @Transactional
    public AuthResponse refresh(String presentedToken) {
        UUID userId = refreshTokenService.getUserId(presentedToken);
        if (userId == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));
        if (user.getStatus() != UserStatus.ACTIVE
                || user.getRefreshTokenExpiresAt() == null
                || !user.getRefreshTokenExpiresAt().isAfter(Instant.now())
                || !refreshTokenService.matches(user.getRefreshTokenHash(), presentedToken)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        List<RoleCode> roles = userRoleRepository.findAllByIdUserId(user.getId()).stream()
                .map(UserRole::getRoleReference)
                .filter(role -> role != null)
                .map(Role::getCode)
                .distinct()
                .sorted(Comparator.comparingInt(AuthServiceImpl::rolePriority))
                .toList();
        if (roles.isEmpty()) {
            throw new AppException(ErrorCode.USER_ACCOUNT_UNAVAILABLE);
        }
        return createAuthSession(user, roles.getFirst());
    }

    @Override
    @Transactional
    public void logout(AuthPrincipal principal) {
        invalidatedTokenRepository.invalidate(principal.tokenId(), principal.expiresAt());
        userRepository.findByIdForUpdate(UUID.fromString(principal.accountId())).ifPresent(user -> {
            user.setRefreshTokenHash(null);
            user.setRefreshTokenExpiresAt(null);
        });
    }

    private AuthResponse createAuthSession(User user, RoleCode role) {
        IssuedToken accessToken = jwtTokenService.issue(user, role);
        var refreshToken = refreshTokenService.issue(user.getId());
        user.setRefreshTokenHash(refreshToken.hash());
        user.setRefreshTokenExpiresAt(refreshToken.expiresAt());
        AuthResponse response = new AuthResponse(
                accessToken.value(),
                "Bearer",
                jwtTokenService.getExpirationSeconds(),
                user.getId().toString(),
                user.getEmail(),
                user.getFullName(),
                role.name().toUpperCase(Locale.ROOT),
                refreshToken.value(),
                refreshToken.expiresAt());
        return response;
    }

    private static int rolePriority(RoleCode role) {
        return switch (role) {
            case ADMIN -> 0;
            case OWNER -> 1;
            case MANAGER -> 2;
            case TENANT -> 3;
            case USER -> 4;
        };
    }
}
