package com.sba.project.modules.identity.service;

import com.sba.project.modules.identity.dto.request.LoginRequest;
import com.sba.project.modules.identity.dto.request.RegisterRequest;
import com.sba.project.modules.identity.dto.response.AuthResponse;
import com.sba.project.common.security.AuthPrincipal;

public interface AuthService {
    void register(RegisterRequest request);

    void verifyEmail(String token);

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(String refreshToken);

    void logout(AuthPrincipal principal);
}
