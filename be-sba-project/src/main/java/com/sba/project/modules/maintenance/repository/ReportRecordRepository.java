package com.sba.project.modules.maintenance.repository;

import com.sba.project.modules.maintenance.dto.response.RepairCostStatResponse;
import com.sba.project.modules.maintenance.entity.ReportRecord;
import com.sba.project.modules.maintenance.enums.ReportRecordApprovalStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReportRecordRepository extends JpaRepository<ReportRecord, UUID> {
    List<ReportRecord> findByReportRequestIdOrderByOccurredAtAsc(UUID reportRequestId);

    Optional<ReportRecord> findByIdAndReportRequestId(UUID id, UUID reportRequestId);

    @Query("""
            select coalesce(sum(r.amount), 0) from ReportRecord r
            where r.parentCostRecordId = :costId and r.approvalStatus in :statuses
            """)
    BigDecimal sumLiabilities(@Param("costId") UUID costId,
            @Param("statuses") Collection<ReportRecordApprovalStatus> statuses);

    /** P2-32 for one room (approved liabilities only). */
    @Query("""
            select new com.sba.project.modules.maintenance.dto.response.RepairCostStatResponse(
                rr.roomId, r.responsibleParty, sum(r.amount), count(r))
            from ReportRecord r, ReportRequest rr
            where rr.id = r.reportRequestId and rr.roomId = :roomId
              and r.recordType = com.sba.project.modules.maintenance.enums.ReportRecordRecordType.LIABILITY
              and r.approvalStatus = com.sba.project.modules.maintenance.enums.ReportRecordApprovalStatus.APPROVED
            group by rr.roomId, r.responsibleParty order by rr.roomId, r.responsibleParty
            """)
    List<RepairCostStatResponse> costStatsByRoom(@Param("roomId") UUID roomId);

    /** P2-32 for every room of a Branch (approved liabilities only). */
    @Query("""
            select new com.sba.project.modules.maintenance.dto.response.RepairCostStatResponse(
                rr.roomId, r.responsibleParty, sum(r.amount), count(r))
            from ReportRecord r, ReportRequest rr, Room rm
            where rr.id = r.reportRequestId and rm.id = rr.roomId and rm.branchId = :branchId
              and r.recordType = com.sba.project.modules.maintenance.enums.ReportRecordRecordType.LIABILITY
              and r.approvalStatus = com.sba.project.modules.maintenance.enums.ReportRecordApprovalStatus.APPROVED
            group by rr.roomId, r.responsibleParty order by rr.roomId, r.responsibleParty
            """)
    List<RepairCostStatResponse> costStatsByBranch(@Param("branchId") UUID branchId);

    /** Compare-and-set PENDING -> decision; approved rows are immutable in the database. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update ReportRecord r set r.approvalStatus = :decision, r.approvedBy = :actor, r.approvedAt = :now
            where r.id = :id and r.approvalStatus = com.sba.project.modules.maintenance.enums.ReportRecordApprovalStatus.PENDING
            """)
    int decide(@Param("id") UUID id, @Param("decision") ReportRecordApprovalStatus decision,
            @Param("actor") UUID actor, @Param("now") Instant now);
}
