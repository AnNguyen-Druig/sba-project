package com.sba.project.modules.maintenance.service.impl;

import com.sba.project.modules.leasing.enums.ContractStatus;
import com.sba.project.modules.leasing.enums.ContractTenantStatus;
import com.sba.project.modules.maintenance.service.RoomTenancyPort;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Read-only lookup over CONTRACT_TENANTS/CONTRACTS (Student 3's data). Only answers "does the user
 * currently rent this room"; contract validation itself stays in leasing.
 */
@Component
@RequiredArgsConstructor
public class ContractRoomTenancyPort implements RoomTenancyPort {
    public static final String QUERY = """
            select ct.id from ContractTenant ct, Contract c
            where c.id = ct.contractId and ct.tenantUserId = :userId and ct.roomId = :roomId
              and ct.status = :memberStatus and c.status = :contractStatus
            order by ct.startDate desc
            """;

    private final EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public Optional<UUID> findActiveContractTenantId(UUID userId, UUID roomId) {
        if (userId == null || roomId == null) return Optional.empty();
        return entityManager.createQuery(QUERY, UUID.class)
                .setParameter("userId", userId).setParameter("roomId", roomId)
                .setParameter("memberStatus", ContractTenantStatus.ACTIVE)
                .setParameter("contractStatus", ContractStatus.ACTIVE)
                .setMaxResults(1)
                .getResultStream().findFirst();
    }
}
