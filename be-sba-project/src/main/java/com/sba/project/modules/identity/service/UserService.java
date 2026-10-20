package com.sba.project.modules.identity.service;

import com.sba.project.modules.identity.dto.request.UserCreateRequest;
import com.sba.project.modules.identity.dto.request.UserActiveStatusRequest;
import com.sba.project.modules.identity.dto.response.UserResponse;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    Page<UserResponse> getUsers(Pageable pageable);

    UserResponse getUserById(UUID id);

    UserResponse createUser(UserCreateRequest request);

    UserResponse setUserActiveStatus(UUID id, UserActiveStatusRequest request);

    void deleteUser(UUID id);
}
