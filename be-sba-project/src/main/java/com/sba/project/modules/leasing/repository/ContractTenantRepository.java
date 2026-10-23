package com.sba.project.modules.leasing.repository;

import com.sba.project.modules.leasing.entity.ContractTenant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContractTenantRepository extends JpaRepository<ContractTenant, UUID> {
    /** ACTIVE member of an ACTIVE main Contract. */
    String ACTIVE_TENANCY = """
            from ContractTenant ct, Contract c
            where c.id = ct.contractId
              and ct.status = com.sba.project.modules.leasing.enums.ContractTenantStatus.ACTIVE
              and c.status = com.sba.project.modules.leasing.enums.ContractStatus.ACTIVE
            """;
    /** BR-R20a/R20b: lease already past its last day but the member has not handed the room back. */
    String OVERSTAYING = ACTIVE_TENANCY + """
              and ct.roomId = :roomId and ct.actualLeaveDate is null and ct.endDate <= :today
            """;

    @Query("select ct.id " + ACTIVE_TENANCY + " and ct.tenantUserId = :userId and ct.roomId = :roomId"
            + " order by ct.startDate desc")
    List<UUID> findActiveTenancyIds(@Param("userId") UUID userId, @Param("roomId") UUID roomId);

    @Query("select count(ct) > 0 " + ACTIVE_TENANCY + " and ct.tenantUserId = :userId and c.branchId = :branchId")
    boolean existsActiveTenancyInBranch(@Param("userId") UUID userId, @Param("branchId") UUID branchId);

    /** Whole-room request: any overstaying member keeps the Room occupied. */
    @Query("select count(ct) > 0 " + OVERSTAYING)
    boolean existsOverstayInRoom(@Param("roomId") UUID roomId, @Param("today") LocalDate today);

    /** Slot request: the same Slot, or a whole-room member (no Slot), keeps it occupied. */
    @Query("select count(ct) > 0 " + OVERSTAYING + " and (ct.roomSlotId is null or ct.roomSlotId = :slotId)")
    boolean existsOverstayForSlot(@Param("roomId") UUID roomId, @Param("slotId") UUID slotId,
            @Param("today") LocalDate today);
}
