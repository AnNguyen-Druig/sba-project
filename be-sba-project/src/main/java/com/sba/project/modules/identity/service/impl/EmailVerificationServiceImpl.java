package com.sba.project.modules.identity.service.impl;

import com.sba.project.common.exceptions.AppException;
import com.sba.project.common.exceptions.ErrorCode;
import com.sba.project.modules.identity.entity.User;
import com.sba.project.modules.identity.enums.UserStatus;
import com.sba.project.modules.identity.enums.VerificationPurpose;
import com.sba.project.modules.identity.repository.UserRepository;
import com.sba.project.modules.identity.service.EmailVerificationService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmailVerificationServiceImpl implements EmailVerificationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailVerificationService.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String INVALID_TOKEN_MESSAGE = "Email verification link is invalid or expired";

    private final UserRepository userRepository;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final String frontendUrl;
    private final String senderAddress;
    private final long expirationMs;

    public EmailVerificationServiceImpl(
            UserRepository userRepository,
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${app.frontend-url:http://localhost:3000}") String frontendUrl,
            @Value("${app.mail.from:no-reply@example.com}") String senderAddress,
            @Value("${app.mail.verification-expiration-ms:86400000}") long expirationMs) {
        this.userRepository = userRepository;
        this.mailSenderProvider = mailSenderProvider;
        this.frontendUrl = frontendUrl.replaceAll("/+$", "");
        this.senderAddress = senderAddress;
        this.expirationMs = expirationMs;
    }

    @Override
    @Transactional
    public void sendVerificationEmail(User user) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Email delivery is not configured on the server");
        }

        String token = createToken();
        user.setVerificationPurpose(VerificationPurpose.EMAIL);
        user.setVerificationSecretHash(hashToken(token));
        user.setVerificationExpiresAt(Instant.now().plusMillis(expirationMs));
        user.setVerificationAttempts(0);
        user.setVerificationMaxAttempts(5);

        userRepository.save(user);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderAddress);
        message.setTo(user.getEmail());
        message.setSubject("Verify your SBA account");
        message.setText("""
                Hello %s,

                Confirm your email address by opening this link:
                %s/verify-email?token=%s

                This link expires in %s. If you did not create this account, you can ignore this email.
                """.formatted(user.getFullName(), frontendUrl, token, formatExpiration()));
        try {
            mailSender.send(message);
        } catch (MailException exception) {
            LOGGER.error("Failed to send email verification message for user {}", user.getId(), exception);
            throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Could not send the verification email");
        }
    }

    @Override
    @Transactional
    public void verifyEmail(String presentedToken) {
        if (presentedToken == null || presentedToken.isBlank()) {
            throw new AppException(ErrorCode.VERIFICATION_INVALID, INVALID_TOKEN_MESSAGE);
        }
        User user = userRepository.findByVerificationSecretHashForUpdate(hashToken(presentedToken))
                .orElseThrow(() -> new AppException(ErrorCode.VERIFICATION_INVALID, INVALID_TOKEN_MESSAGE));
        Instant now = Instant.now();
        if (user.getVerificationPurpose() != VerificationPurpose.EMAIL
                || user.getVerificationExpiresAt() == null
                || !user.getVerificationExpiresAt().isAfter(now)
                || user.getVerificationConsumedAt() != null
                || user.getEmailVerifiedAt() != null
                || user.getStatus() != UserStatus.PENDING_VERIFICATION) {
            throw new AppException(ErrorCode.VERIFICATION_INVALID, INVALID_TOKEN_MESSAGE);
        }

        user.setEmailVerifiedAt(now);
        user.setStatus(UserStatus.ACTIVE);
        user.setVerificationSecretHash(null);
        user.setVerificationPurpose(null);
        user.setVerificationExpiresAt(null);
        user.setVerificationMaxAttempts(null);
        user.setVerificationConsumedAt(null);
    }

    private static String createToken() {
        byte[] tokenBytes = new byte[32];
        SECURE_RANDOM.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    private static String hashToken(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    private String formatExpiration() {
        Duration expiration = Duration.ofMillis(expirationMs);
        long hours = expiration.toHours();
        long minutes = expiration.toMinutes();
        if (minutes > 0 && minutes % 60 == 0) {
            return hours + (hours == 1 ? " hour" : " hours");
        }
        return minutes + (minutes == 1 ? " minute" : " minutes");
    }
}
