package com.sba.project;

import com.sba.project.modules.property.entity.Branch;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests the shared JPA lifecycle callbacks without requiring an external database. */
class BaseEntityPersistenceTest {
    @Test
    void prePersist_populatesBothTimestamps() {
        Branch branch = Branch.builder().name("Central").build();
        Instant before = Instant.now();

        ReflectionTestUtils.invokeMethod(branch, "onCreate");

        assertNotNull(branch.getCreatedAt());
        assertEquals(branch.getCreatedAt(), branch.getUpdatedAt());
        assertFalse(branch.getCreatedAt().isBefore(before));
        assertFalse(branch.getCreatedAt().isAfter(Instant.now()));
    }

    @Test
    void prePersist_preservesImportedTimestamps() {
        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
        Instant updatedAt = createdAt.plusSeconds(60);
        Branch branch = Branch.builder().name("Central")
                .createdAt(createdAt).updatedAt(updatedAt).build();

        ReflectionTestUtils.invokeMethod(branch, "onCreate");

        assertEquals(createdAt, branch.getCreatedAt());
        assertEquals(updatedAt, branch.getUpdatedAt());
    }

    @Test
    void preUpdate_advancesUpdatedAtWithoutChangingCreatedAt() {
        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
        Instant updatedAt = createdAt.plusSeconds(60);
        Branch branch = Branch.builder().name("Central")
                .createdAt(createdAt).updatedAt(updatedAt).build();
        branch.setName("Central Updated");
        Instant before = Instant.now();

        ReflectionTestUtils.invokeMethod(branch, "onUpdate");

        assertEquals(createdAt, branch.getCreatedAt());
        assertTrue(branch.getUpdatedAt().isAfter(updatedAt));
        assertFalse(branch.getUpdatedAt().isBefore(before));
        assertFalse(branch.getUpdatedAt().isAfter(Instant.now()));
    }
}
