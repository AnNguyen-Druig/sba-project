package com.sba.project.modules.identity.service;

import com.sba.project.modules.identity.entity.User;

public interface EmailVerificationService {
    void sendVerificationEmail(User user);

    void verifyEmail(String token);
}
