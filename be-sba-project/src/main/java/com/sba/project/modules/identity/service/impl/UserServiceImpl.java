package com.sba.project.modules.identity.service.impl;

import com.sba.project.common.exceptions.AppException;
import com.sba.project.common.exceptions.ErrorCode;
import com.sba.project.modules.identity.dto.request.UserActiveStatusRequest;
import com.sba.project.modules.identity.dto.request.UserCreateRequest;
import com.sba.project.modules.identity.dto.response.UserResponse;
import com.sba.project.modules.identity.entity.User;
import com.sba.project.modules.identity.enums.UserStatus;
import com.sba.project.modules.identity.repository.UserRepository;
import com.sba.project.modules.identity.service.UserService;
import java.util.Locale;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserServiceImpl::toUserResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        return toUserResponse(findUserById(id));
    }

    @Override
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        String email = normalizeUserEmail(request.getEmail());
        String phone = normalizeUserPhone(request.getPhone());
        validateUserContactProvided(email, phone);
        validateUserContactsUnique(email, phone, null);

        User user = new User();
        user.setEmail(email);
        user.setPhone(phone);
        user.setFullName(request.getFullName().trim());
        user.setStatus(UserStatus.INVITED);
        user.setDateOfBirth(request.getDateOfBirth());
        user.setGender(normalizeOptionalField(request.getGender()));
        return toUserResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse setUserActiveStatus(UUID id, UserActiveStatusRequest request) {
        User user = findUserById(id);
        if (request.getActive()) {
            if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
                throw new AppException(ErrorCode.USER_ACCOUNT_UNAVAILABLE);
            }
            if (user.getStatus() == UserStatus.PENDING_VERIFICATION && user.getEmailVerifiedAt() == null) {
                throw new AppException(ErrorCode.EMAIL_NOT_VERIFIED);
            }
            user.setStatus(UserStatus.ACTIVE);
        } else {
            user.setStatus(UserStatus.DISABLED);
        }
        return toUserResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteUser(UUID id) {
        User user = findUserById(id);
        userRepository.delete(user);
    }

    private User findUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private void validateUserContactsUnique(String email, String phone, UUID currentId) {
        if (email != null && (currentId == null
                ? userRepository.existsByEmailIgnoreCase(email)
                : userRepository.existsByEmailIgnoreCaseAndIdNot(email, currentId))) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        if (phone != null && (currentId == null
                ? userRepository.existsByPhone(phone)
                : userRepository.existsByPhoneAndIdNot(phone, currentId))) {
            throw new AppException(ErrorCode.PHONE_ALREADY_EXISTS);
        }
    }

    private void validateUserContactProvided(String email, String phone) {
        if (email == null && phone == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
    }

    private static String normalizeUserEmail(String email) {
        if (email == null || email.isBlank()) return null;
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizeUserPhone(String phone) {
        if (phone == null || phone.isBlank()) return null;
        return phone.trim();
    }

    private static String normalizeOptionalField(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }

    private static UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getPhone(),
                user.getFullName(),
                user.getStatus(),
                user.getCreatedAt());
    }
}
