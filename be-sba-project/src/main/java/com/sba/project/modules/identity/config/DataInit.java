package com.sba.project.modules.identity.config;

import com.sba.project.modules.identity.entity.Role;
import com.sba.project.modules.identity.entity.User;
import com.sba.project.modules.identity.entity.UserRole;
import com.sba.project.modules.identity.entity.UserRoleId;
import com.sba.project.modules.identity.enums.RoleCode;
import com.sba.project.modules.identity.enums.UserStatus;
import com.sba.project.modules.identity.repository.RoleRepository;
import com.sba.project.modules.identity.repository.UserRepository;
import com.sba.project.modules.identity.repository.UserRoleRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInit {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner initAdminAccount() {
        return args -> createAdminIfNotExists();
    }

    @Transactional
    protected void createAdminIfNotExists() {
        // Check if ADMIN role exists, create if not
        Role adminRole = roleRepository.findByCode(RoleCode.ADMIN)
                .orElseGet(() -> {
                    log.info("Creating ADMIN role...");
                    return roleRepository.save(Role.builder().code(RoleCode.ADMIN).build());
                });

        // Check if admin account already exists
        if (userRepository.existsByEmailIgnoreCase("admin@gmail.com")) {
            log.info("Admin account already exists. Skipping initialization.");
            return;
        }

        // Create admin user
        log.info("Admin account not found. Creating default admin account...");
        User adminUser = User.builder()
                .email("admin@gmail.com")
                .passwordHash(passwordEncoder.encode("admin123456"))
                .fullName("Administrator")
                .status(UserStatus.ACTIVE)
                .emailVerifiedAt(Instant.now())
                .build();
        adminUser = userRepository.save(adminUser);

        // Assign ADMIN role to the user
        UserRole userRole = UserRole.builder()
                .id(new UserRoleId(adminUser.getId(), adminRole.getId()))
                .assignedAt(Instant.now())
                .build();
        userRoleRepository.save(userRole);

        log.info("Default admin account created successfully (email: admin@gmail.com).");
    }
}
