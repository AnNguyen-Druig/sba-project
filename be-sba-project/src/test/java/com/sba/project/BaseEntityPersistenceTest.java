package com.sba.project;

import com.sba.project.entity.Branch;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DataJpaTest(properties = "spring.config.import=")
@ActiveProfiles("test")
class BaseEntityPersistenceTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    void persistAndUpdate_populatesTimestamps() {
        Branch branch = Branch.builder().branchName("Central").build();
        entityManager.persist(branch);
        entityManager.flush();
        entityManager.clear();

        Branch persisted = entityManager.find(Branch.class, branch.getBranchId());
        assertNotNull(persisted.getCreatedAt());
        assertNotNull(persisted.getUpdatedAt());
        var originalUpdatedAt = persisted.getUpdatedAt();

        persisted.setBranchName("Central Updated");
        entityManager.flush();
        entityManager.clear();

        Branch updated = entityManager.find(Branch.class, branch.getBranchId());
        assertFalse(updated.getUpdatedAt().isBefore(originalUpdatedAt));
    }
}
