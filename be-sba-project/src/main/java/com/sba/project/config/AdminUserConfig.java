package com.sba.project.config;

import com.sba.project.entity.User;
import com.sba.project.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Creates an initial administrator only when explicitly configured by the operator. */
@Configuration
@ConditionalOnProperty(name = "app.admin.bootstrap-enabled", havingValue = "true")
@Slf4j
public class AdminUserConfig {

    /** Seeds the first administrator using credentials supplied outside source control. */
    @Bean
    public ApplicationRunner initializeAdminUser(UserRepository userRepository, PasswordEncoder passwordEncoder,
            @Value("${ADMIN_EMAIL}") String email, @Value("${ADMIN_PASSWORD}") String password) {
        return args -> {
            if (email.isBlank() || password.isBlank()) {
                throw new IllegalArgumentException("Admin bootstrap credentials must not be blank");
            }
            if (userRepository.existsByRole(User.Role.ADMIN)) {
                return;
            }
            if (userRepository.existsByEmail(email)) {
                throw new IllegalStateException("Configured admin email exists but is not an ADMIN user");
            }
            User admin = User.builder()
                    .email(email)
                    .password(passwordEncoder.encode(password))
                    .role(User.Role.ADMIN)
                    .build();
            userRepository.save(admin);
            log.info("Created initial admin user: {}", admin.getEmail());
        };
    }
}
