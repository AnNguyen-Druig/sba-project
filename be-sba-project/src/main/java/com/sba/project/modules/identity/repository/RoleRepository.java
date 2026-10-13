package com.sba.project.modules.identity.repository;

import com.sba.project.modules.identity.entity.Role;
import com.sba.project.modules.identity.enums.RoleCode;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByCode(RoleCode code);
}
