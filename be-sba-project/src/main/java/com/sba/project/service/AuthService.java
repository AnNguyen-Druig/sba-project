package com.sba.project.service;

import com.sba.project.dto.request.LoginRequest;
import com.sba.project.dto.request.RegisterRequest;
import com.sba.project.dto.request.RefreshTokenRequest;
import com.sba.project.dto.response.AuthResponse;
import com.sba.project.dto.response.UserResponse;
import com.sba.project.entity.InvalidateToken;
import com.sba.project.entity.User;
import com.sba.project.exception.BadRequestException;
import com.sba.project.repository.InvalidateTokenRepository;
import com.sba.project.repository.UserRepository;
import com.sba.project.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final InvalidateTokenRepository invalidateTokenRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email is already registered");
        }
        String phone = request.getPhone() == null || request.getPhone().isBlank()
                ? null
                : request.getPhone().trim();
        if (phone != null && userRepository.existsByPhone(phone)) {
            throw new BadRequestException("Phone is already registered");
        }

        User user = User.builder()
                .email(email)
                .phone(phone)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.USER)
                .build();
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword()));
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new BadRequestException("User no longer exists"));
        return AuthResponse.of(
                tokenProvider.generateToken(authentication),
                tokenProvider.generateRefreshToken(user),
                tokenProvider.getAccessExpirationMs(),
                UserResponse.from(user)
        );
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        validateUsableToken(refreshToken, true);
        String username = tokenProvider.getUsernameFromJWT(refreshToken);
        invalidateToken(refreshToken);

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new BadRequestException("User no longer exists"));
        return issueTokens(user);
    }

    @Transactional
    public void logout(String accessToken, RefreshTokenRequest request) {
        validateUsableToken(accessToken, false);
        invalidateToken(accessToken);
        if (request != null && request.getRefreshToken() != null && !request.getRefreshToken().isBlank()) {
            String refreshToken = request.getRefreshToken();
            validateUsableToken(refreshToken, true);
            invalidateToken(refreshToken);
        }
    }

    private AuthResponse issueTokens(User user) {
        return AuthResponse.of(
                tokenProvider.generateAccessToken(user),
                tokenProvider.generateRefreshToken(user),
                tokenProvider.getAccessExpirationMs(),
                UserResponse.from(user)
        );
    }

    private void validateUsableToken(String token, boolean refreshToken) {
        if (!tokenProvider.validateToken(token)) {
            throw new BadRequestException("Token is invalid or expired");
        }
        String tokenId = tokenProvider.getTokenIdFromJWT(token);
        boolean expectedType = refreshToken
                ? tokenProvider.isRefreshToken(token)
                : tokenProvider.isAccessToken(token);
        if (!expectedType || tokenId == null || invalidateTokenRepository.existsById(tokenId)) {
            throw new BadRequestException("Token is invalid or has been invalidated");
        }
    }

    private void invalidateToken(String token) {
        invalidateTokenRepository.save(InvalidateToken.builder()
                .id(tokenProvider.getTokenIdFromJWT(token))
                .expiredTime(LocalDateTime.ofInstant(
                        tokenProvider.getExpirationFromJWT(token).toInstant(), ZoneOffset.UTC))
                .build());
    }
}
