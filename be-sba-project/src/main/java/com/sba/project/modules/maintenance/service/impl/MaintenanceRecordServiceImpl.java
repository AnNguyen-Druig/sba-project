package com.sba.project.modules.maintenance.service.impl;

import com.sba.project.common.audit.AuditActorContext;
import com.sba.project.common.exceptions.AppException;
import com.sba.project.common.exceptions.ErrorCode;
import com.sba.project.modules.maintenance.dto.request.LiabilityRequest;
import com.sba.project.modules.maintenance.dto.request.MaintenanceRecordRequest;
import com.sba.project.modules.maintenance.dto.response.MaintenanceRecordResponse;
import com.sba.project.modules.maintenance.entity.ReportRecord;
import com.sba.project.modules.maintenance.entity.ReportRequest;
import com.sba.project.modules.maintenance.enums.ReportRecordApprovalStatus;
import com.sba.project.modules.maintenance.enums.ReportRecordRecordType;
import com.sba.project.modules.maintenance.enums.ReportRecordResponsibleParty;
import com.sba.project.modules.maintenance.mapper.MaintenanceRequestMapper;
import com.sba.project.modules.maintenance.repository.ReportRecordRepository;
import com.sba.project.modules.maintenance.service.MaintenanceRecordService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.EnumSet;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cost/liability rules come from the schema: a LIABILITY splits one COST between OWNER and TENANT,
 * approved liabilities must add up to the approved cost, and approved rows are immutable. Student 5
 * bills only approved TENANT liabilities; nothing here touches invoices.
 */
@Service
@RequiredArgsConstructor
public class MaintenanceRecordServiceImpl implements MaintenanceRecordService {
    private static final EnumSet<ReportRecordApprovalStatus> PENDING_OR_APPROVED =
            EnumSet.of(ReportRecordApprovalStatus.PENDING, ReportRecordApprovalStatus.APPROVED);
    private static final EnumSet<ReportRecordApprovalStatus> APPROVED_ONLY =
            EnumSet.of(ReportRecordApprovalStatus.APPROVED);

    private final ReportRecordRepository recordRepository;
    private final MaintenanceRequestMapper mapper;
    private final MaintenanceRequestAccess access;
    private final AuditActorContext auditActor;

    @Override
    @Transactional
    public MaintenanceRecordResponse addRecord(UUID requestId, MaintenanceRecordRequest command) {
        MaintenanceRequestAccess.Participant manager = access.requireManager(requestId);
        requireOpenForRecords(manager.request());
        ReportRecordRecordType type = command.recordType();
        if (type != ReportRecordRecordType.WORK && type != ReportRecordRecordType.COST) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        boolean cost = type == ReportRecordRecordType.COST;
        if (cost != (command.amount() != null)) throw new AppException(ErrorCode.INVALID_REQUEST);
        auditActor.bind(manager.actorId());
        ReportRecord record = recordRepository.save(ReportRecord.builder().reportRequestId(requestId)
                .recordType(type).description(command.description().trim()).amount(command.amount())
                .performedBy(manager.actorId()).statusAfter(command.statusAfter())
                .approvalStatus(cost ? ReportRecordApprovalStatus.PENDING : null)
                .evidenceRefs(EvidenceReferences.array(command.evidence(), manager.actorId())).build());
        return mapper.toRecord(record);
    }

    @Override
    @Transactional
    public MaintenanceRecordResponse addLiability(UUID requestId, UUID costRecordId, LiabilityRequest command) {
        MaintenanceRequestAccess.Participant manager = access.requireManager(requestId);
        ReportRequest request = manager.request();
        requireOpenForRecords(request);
        ReportRecord cost = loadRecord(requestId, costRecordId);
        if (cost.getRecordType() != ReportRecordRecordType.COST
                || cost.getApprovalStatus() != ReportRecordApprovalStatus.PENDING) {
            throw new AppException(ErrorCode.MAINTENANCE_STATE_CONFLICT);
        }
        boolean tenant = command.responsibleParty() == ReportRecordResponsibleParty.TENANT;
        if (tenant && request.getContractTenantId() == null) {
            throw new AppException(ErrorCode.MAINTENANCE_STATE_CONFLICT);
        }
        BigDecimal committed = recordRepository.sumLiabilities(costRecordId, PENDING_OR_APPROVED);
        if (committed.add(command.amount()).compareTo(cost.getAmount()) > 0) {
            throw new AppException(ErrorCode.MAINTENANCE_LIABILITY_EXCEEDS_COST);
        }
        auditActor.bind(manager.actorId());
        ReportRecord liability = recordRepository.save(ReportRecord.builder().reportRequestId(requestId)
                .recordType(ReportRecordRecordType.LIABILITY).parentCostRecordId(costRecordId)
                .responsibleParty(command.responsibleParty())
                .contractTenantId(tenant ? request.getContractTenantId() : null)
                .amount(command.amount()).description(command.description().trim())
                .performedBy(manager.actorId()).approvalStatus(ReportRecordApprovalStatus.PENDING).build());
        return mapper.toRecord(liability);
    }

    @Override
    @Transactional
    public MaintenanceRecordResponse approveRecord(UUID requestId, UUID recordId) {
        MaintenanceRequestAccess.Participant manager = access.requireManager(requestId);
        ReportRecord record = loadDecidable(requestId, recordId);
        if (record.getRecordType() == ReportRecordRecordType.COST) {
            BigDecimal approved = recordRepository.sumLiabilities(recordId, APPROVED_ONLY);
            if (approved.compareTo(record.getAmount()) != 0) {
                throw new AppException(ErrorCode.MAINTENANCE_COST_LIABILITY_MISMATCH);
            }
        } else {
            // Liabilities of an already approved cost are frozen (approved sum must stay equal to the cost).
            ReportRecord cost = loadRecord(requestId, record.getParentCostRecordId());
            if (cost.getApprovalStatus() != ReportRecordApprovalStatus.PENDING) {
                throw new AppException(ErrorCode.MAINTENANCE_STATE_CONFLICT);
            }
        }
        return decide(manager.actorId(), recordId, requestId, ReportRecordApprovalStatus.APPROVED);
    }

    @Override
    @Transactional
    public MaintenanceRecordResponse rejectRecord(UUID requestId, UUID recordId) {
        MaintenanceRequestAccess.Participant manager = access.requireManager(requestId);
        loadDecidable(requestId, recordId);
        return decide(manager.actorId(), recordId, requestId, ReportRecordApprovalStatus.REJECTED);
    }

    private MaintenanceRecordResponse decide(UUID actorId, UUID recordId, UUID requestId,
            ReportRecordApprovalStatus decision) {
        auditActor.bind(actorId);
        if (recordRepository.decide(recordId, decision, actorId, Instant.now()) == 0) {
            throw new AppException(ErrorCode.MAINTENANCE_STATE_CONFLICT);
        }
        return mapper.toRecord(loadRecord(requestId, recordId));
    }

    private ReportRecord loadDecidable(UUID requestId, UUID recordId) {
        ReportRecord record = loadRecord(requestId, recordId);
        boolean financial = record.getRecordType() == ReportRecordRecordType.COST
                || record.getRecordType() == ReportRecordRecordType.LIABILITY;
        if (!financial || record.getApprovalStatus() != ReportRecordApprovalStatus.PENDING) {
            throw new AppException(ErrorCode.MAINTENANCE_STATE_CONFLICT);
        }
        return record;
    }

    private ReportRecord loadRecord(UUID requestId, UUID recordId) {
        return recordRepository.findByIdAndReportRequestId(recordId, requestId)
                .orElseThrow(() -> new AppException(ErrorCode.REPORT_RECORD_NOT_FOUND));
    }

    private static void requireOpenForRecords(ReportRequest request) {
        if (MaintenanceRequestServiceImpl.CLOSED_STATUSES.contains(request.getStatus())) {
            throw new AppException(ErrorCode.MAINTENANCE_STATE_CONFLICT);
        }
    }
}
