package com.sba.project.modules.identity.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.sba.project.common.exceptions.AppException;
import com.sba.project.common.exceptions.ErrorCode;
import com.sba.project.modules.identity.dto.request.UserActiveStatusRequest;
import com.sba.project.modules.identity.dto.request.UserCreateRequest;
import com.sba.project.modules.identity.entity.User;
import com.sba.project.modules.identity.enums.UserStatus;
import com.sba.project.modules.identity.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserServiceImplTests {
    private UserRepository userRepository;
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userService = new UserServiceImpl(userRepository);
    }

    @Test
    void createNormalizesContactAndCreatesInvitedUser() {
        UserCreateRequest request = new UserCreateRequest();
        request.setEmail("  New.User@Example.com ");
        request.setPhone(" +84901234567 ");
        request.setFullName("  New User  ");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });

        var response = userService.createUser(request);

        assertThat(response.email()).isEqualTo("new.user@example.com");
        assertThat(response.phone()).isEqualTo("+84901234567");
        assertThat(response.fullName()).isEqualTo("New User");
        assertThat(response.status()).isEqualTo(UserStatus.INVITED);
        var userCaptor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPasswordHash()).isNull();
        assertThat(userCaptor.getValue().getRefreshTokenHash()).isNull();
    }

    @Test
    void createRejectsExistingEmail() {
        UserCreateRequest request = new UserCreateRequest();
        request.setEmail("user@example.com");
        request.setFullName("User");
        when(userRepository.existsByEmailIgnoreCase("user@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(AppException.class)
                .extracting(exception -> ((AppException) exception).getErrorCode())
                .isEqualTo(ErrorCode.EMAIL_ALREADY_EXISTS);
        verify(userRepository, never()).save(any());
    }

    @Test
    void setUserActiveStatusDisablesAccountWithoutChangingBasicInfo() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setEmail("user@example.com");
        user.setPhone("+84901234567");
        user.setFullName("User Name");
        user.setStatus(UserStatus.ACTIVE);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        UserActiveStatusRequest request = new UserActiveStatusRequest();
        request.setActive(false);

        var response = userService.setUserActiveStatus(userId, request);

        assertThat(response.status()).isEqualTo(UserStatus.DISABLED);
        assertThat(response.fullName()).isEqualTo("User Name");
        assertThat(response.phone()).isEqualTo("+84901234567");
        assertThat(response.email()).isEqualTo("user@example.com");
        assertThat(user.getFullName()).isEqualTo("User Name");
        assertThat(user.getPhone()).isEqualTo("+84901234567");
    }

    @Test
    void setUserActiveStatusRejectsActivationWithoutPassword() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setStatus(UserStatus.INVITED);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        UserActiveStatusRequest request = new UserActiveStatusRequest();
        request.setActive(true);

        assertThatThrownBy(() -> userService.setUserActiveStatus(userId, request))
                .isInstanceOf(AppException.class)
                .extracting(exception -> ((AppException) exception).getErrorCode())
                .isEqualTo(ErrorCode.USER_ACCOUNT_UNAVAILABLE);
        verify(userRepository, never()).save(any());
    }

    @Test
    void setUserActiveStatusCannotBypassPendingEmailVerification() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setStatus(UserStatus.PENDING_VERIFICATION);
        user.setPasswordHash("encoded-password");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        UserActiveStatusRequest request = new UserActiveStatusRequest();
        request.setActive(true);

        assertThatThrownBy(() -> userService.setUserActiveStatus(userId, request))
                .isInstanceOf(AppException.class)
                .extracting(exception -> ((AppException) exception).getErrorCode())
                .isEqualTo(ErrorCode.EMAIL_NOT_VERIFIED);
        verify(userRepository, never()).save(any());
    }

    @Test
    void findByIdReturnsNotFoundForMissingUser() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(AppException.class)
                .extracting(exception -> ((AppException) exception).getErrorCode())
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }
}
