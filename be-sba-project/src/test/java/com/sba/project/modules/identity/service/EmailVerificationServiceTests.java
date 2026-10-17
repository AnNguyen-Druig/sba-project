package com.sba.project.modules.identity.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sba.project.common.exceptions.AppException;
import com.sba.project.common.exceptions.ErrorCode;
import com.sba.project.modules.identity.entity.User;
import com.sba.project.modules.identity.enums.UserStatus;
import com.sba.project.modules.identity.enums.VerificationPurpose;
import com.sba.project.modules.identity.repository.UserRepository;
import com.sba.project.modules.identity.service.impl.EmailVerificationServiceImpl;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

class EmailVerificationServiceTests {
    private UserRepository userRepository;
    private ObjectProvider<JavaMailSender> mailSenderProvider;
    private JavaMailSender mailSender;
    private EmailVerificationServiceImpl emailVerificationService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        mailSenderProvider = mock(ObjectProvider.class);
        mailSender = mock(JavaMailSender.class);
        when(mailSenderProvider.getIfAvailable()).thenReturn(mailSender);
        emailVerificationService = new EmailVerificationServiceImpl(
                userRepository, mailSenderProvider, "http://localhost:3000/", "no-reply@example.com", 86_400_000);
    }

    @Test
    void sendsLinkAndStoresOnlyHashOfVerificationToken() {
        User user = new User();
        user.setId(java.util.UUID.randomUUID());
        user.setEmail("user@example.com");
        user.setFullName("Test User");

        emailVerificationService.sendVerificationEmail(user);

        var messageCaptor = org.mockito.ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        String text = String.join("", messageCaptor.getValue().getText());
        Matcher tokenMatcher = Pattern.compile("verify-email\\?token=([A-Za-z0-9_-]+)").matcher(text);
        assertThat(tokenMatcher.find()).isTrue();
        assertThat(text).contains("http://localhost:3000/verify-email");
        assertThat(user.getVerificationSecretHash()).isNotEqualTo(tokenMatcher.group(1));
        assertThat(user.getVerificationPurpose()).isEqualTo(VerificationPurpose.EMAIL);
        assertThat(user.getVerificationExpiresAt()).isNotNull();
        verify(userRepository).save(user);
        verify(userRepository, never()).findById(user.getId());
    }

    @Test
    void verifiesPendingAccountOnceAndActivatesIt() {
        String token = "single-use-token";
        User user = new User();
        user.setStatus(UserStatus.PENDING_VERIFICATION);
        user.setVerificationPurpose(VerificationPurpose.EMAIL);
        user.setVerificationExpiresAt(java.time.Instant.now().plusSeconds(600));
        when(userRepository.findByVerificationSecretHashForUpdate(hash("single-use-token")))
                .thenReturn(Optional.of(user));

        emailVerificationService.verifyEmail(token);

        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.getEmailVerifiedAt()).isNotNull();
        assertThat(user.getVerificationSecretHash()).isNull();
        assertThat(user.getVerificationPurpose()).isNull();
        assertThat(user.getVerificationExpiresAt()).isNull();
        assertThat(user.getVerificationMaxAttempts()).isNull();
        assertThat(user.getVerificationConsumedAt()).isNull();
    }

    @Test
    void rejectsUnknownToken() {
        when(userRepository.findByVerificationSecretHashForUpdate(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> emailVerificationService.verifyEmail("unknown"))
                .isInstanceOf(AppException.class)
                .extracting(exception -> ((AppException) exception).getErrorCode())
                .isEqualTo(ErrorCode.VERIFICATION_INVALID);
    }

    private static String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
