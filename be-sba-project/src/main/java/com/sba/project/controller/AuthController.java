package com.sba.project.controller;

import com.sba.project.dto.request.LoginRequest;
import com.sba.project.dto.request.RefreshTokenRequest;
import com.sba.project.dto.request.RegisterRequest;
import com.sba.project.dto.response.AuthApiResponse;
import com.sba.project.dto.response.AuthResponse;
import com.sba.project.dto.response.UserResponse;
import com.sba.project.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AuthApiResponse.success("User registered.", authService.register(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(AuthApiResponse.success("Login successful.", authService.login(request)));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(AuthApiResponse.success("Token refreshed.", authService.refresh(request)));
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthApiResponse<Void>> logout(
            @RequestHeader("Authorization") String authorization,
            @RequestBody(required = false) RefreshTokenRequest request) {
        authService.logout(authorization.substring("Bearer ".length()), request);
        return ResponseEntity.ok(AuthApiResponse.success("Logout successful.", null));
    }
}
