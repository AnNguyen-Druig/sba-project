package com.sba.project.config;

import com.sba.project.entity.User;
import com.sba.project.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Slf4j
public class AdminUserConfig {

    @Bean
    public ApplicationRunner initializeAdminUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.existsByRole(User.Role.ADMIN)) {
                return;
            }
            if (userRepository.existsByEmail("admin@gmail.com")) {
                throw new IllegalStateException("admin@gmail.com exists but is not an ADMIN user");
            }
            User admin = User.builder()
                    .email("admin@gmail.com")
                    .password(passwordEncoder.encode("admin"))
                    .role(User.Role.ADMIN)
                    .build();
            userRepository.save(admin);
            log.info("Created initial admin user: {}", admin.getEmail());
        };
    }
}
