package com.sba.project.common.security;

import jakarta.persistence.EntityManager;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Owner/Manager Branch scope (BR-03, P1-134): the user is the Branch Owner or its assigned Manager.
 * Shared by the module access policies so none of them re-implements the rule.
 */
@Component
@RequiredArgsConstructor
public class BranchScope {
    public static final String BRANCH_QUERY = """
            select count(b) from Branch b
            where b.id = :branchId and (b.ownerId = :userId or b.managerId = :userId)
            """;
    public static final String ROOM_QUERY = """
            select count(r) from Room r, Branch b
            where r.id = :roomId and b.id = r.branchId
              and (b.ownerId = :userId or b.managerId = :userId)
            """;

    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public boolean canManageBranch(UUID userId, UUID branchId) {
        if (userId == null || branchId == null) return false;
        return entityManager.createQuery(BRANCH_QUERY, Long.class)
                .setParameter("branchId", branchId).setParameter("userId", userId)
                .getSingleResult() > 0;
    }

    @Transactional(readOnly = true)
    public boolean canManageRoom(UUID userId, UUID roomId) {
        if (userId == null || roomId == null) return false;
        return entityManager.createQuery(ROOM_QUERY, Long.class)
                .setParameter("roomId", roomId).setParameter("userId", userId)
                .getSingleResult() > 0;
    }
}
