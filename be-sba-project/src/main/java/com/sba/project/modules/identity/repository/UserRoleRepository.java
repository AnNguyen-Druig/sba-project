package com.sba.project.modules.identity.repository;

import com.sba.project.modules.identity.entity.UserRole;
import com.sba.project.modules.identity.entity.UserRoleId;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {
    List<UserRole> findAllByIdUserId(UUID userId);
}
