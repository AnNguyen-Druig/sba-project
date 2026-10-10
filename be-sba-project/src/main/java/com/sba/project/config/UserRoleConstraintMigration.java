package com.sba.project.config;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class UserRoleConstraintMigration implements ApplicationRunner {

    private final EntityManager entityManager;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        entityManager.createNativeQuery(
                "ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check")
                .executeUpdate();
        entityManager.createNativeQuery(
                "ALTER TABLE users ADD CONSTRAINT users_role_check " +
                        "CHECK (role IN ('ADMIN', 'OWNER', 'MANAGER', 'TENANT', 'USER'))")
                .executeUpdate();
    }
}
