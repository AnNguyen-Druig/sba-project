package com.sba.project.modules.identity.service;

import com.sba.project.modules.identity.dto.request.LoginRequest;
import com.sba.project.modules.identity.dto.request.RegisterRequest;
import com.sba.project.common.security.AuthPrincipal;

public interface AuthService {
    AuthSessionResult register(RegisterRequest request);

    AuthSessionResult login(LoginRequest request);

    AuthSessionResult refresh(String refreshToken);

    void logout(AuthPrincipal principal);
}
