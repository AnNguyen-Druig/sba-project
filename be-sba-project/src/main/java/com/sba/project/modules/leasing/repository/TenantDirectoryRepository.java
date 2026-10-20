package com.sba.project.modules.leasing.repository;

import com.sba.project.modules.leasing.dto.response.TenantResponse;
import com.sba.project.modules.leasing.entity.ContractTenant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/**
 * READ-ONLY tenant lookup for BE-06.2 (P1-31, P1-32, P1-36). Writes to contract_tenants belong to
 * Student 3's leasing services; this repository deliberately exposes no save/delete.
 */
public interface TenantDirectoryRepository extends Repository<ContractTenant, UUID> {
    String PROJECTION = """
            select new com.sba.project.modules.leasing.dto.response.TenantResponse(
                ct.id, ct.contractId, c.branchId, ct.roomId, ct.roomSlotId, ct.tenantUserId, u.fullName,
                ct.role, ct.status, ct.startDate, ct.endDate, ct.actualLeaveDate)
            from ContractTenant ct, Contract c, User u
            where c.id = ct.contractId and u.id = ct.tenantUserId
            """;
    String COUNT = """
            select count(ct) from ContractTenant ct, Contract c
            where c.id = ct.contractId
            """;
    String ORDER = " order by ct.startDate desc, ct.id";

    Optional<ContractTenant> findById(UUID id);

    @Query(value = PROJECTION + " and c.branchId = :branchId" + ORDER,
            countQuery = COUNT + " and c.branchId = :branchId")
    Page<TenantResponse> findByBranch(@Param("branchId") UUID branchId, Pageable pageable);

    @Query(value = PROJECTION + " and ct.roomId = :roomId" + ORDER,
            countQuery = COUNT + " and ct.roomId = :roomId")
    Page<TenantResponse> findByRoom(@Param("roomId") UUID roomId, Pageable pageable);

    @Query(value = PROJECTION + " and ct.contractId = :contractId" + ORDER,
            countQuery = COUNT + " and ct.contractId = :contractId")
    Page<TenantResponse> findByContract(@Param("contractId") UUID contractId, Pageable pageable);

    @Query(value = PROJECTION + " and ct.tenantUserId = :userId" + ORDER,
            countQuery = COUNT + " and ct.tenantUserId = :userId")
    Page<TenantResponse> findByTenantUser(@Param("userId") UUID userId, Pageable pageable);

    @Query("select c.branchId from Contract c where c.id = :contractId")
    Optional<UUID> findBranchIdByContractId(@Param("contractId") UUID contractId);
}
