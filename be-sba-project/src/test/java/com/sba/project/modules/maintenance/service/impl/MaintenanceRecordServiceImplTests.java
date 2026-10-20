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
import com.sba.project.modules.maintenance.enums.ReportRequestPriority;
import com.sba.project.modules.maintenance.enums.ReportRequestStatus;
import com.sba.project.modules.maintenance.mapper.MaintenanceRequestMapperImpl;
import com.sba.project.modules.maintenance.repository.ReportRecordRepository;
import com.sba.project.modules.maintenance.repository.ReportRequestRepository;
import com.sba.project.modules.maintenance.service.MaintenanceAccessPolicy;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MaintenanceRecordServiceImplTests {
    private static final UUID MANAGER = UUID.randomUUID();
    private static final UUID ROOM = UUID.randomUUID();
    private static final UUID TENANCY = UUID.randomUUID();

    @Mock ReportRequestRepository requestRepository;
    @Mock ReportRecordRepository recordRepository;
    @Mock MaintenanceAccessPolicy accessPolicy;
    @Mock AuditActorContext auditActor;
    MaintenanceRecordServiceImpl service;
    ReportRequest request;

    @BeforeEach
    void setUp() {
        service = new MaintenanceRecordServiceImpl(recordRepository, new MaintenanceRequestMapperImpl(),
                new MaintenanceRequestAccess(requestRepository, accessPolicy), auditActor);
        request = ReportRequest.builder().id(UUID.randomUUID()).roomId(ROOM).requesterUserId(UUID.randomUUID())
                .contractTenantId(TENANCY).title("t").description("d").priority(ReportRequestPriority.NORMAL)
                .status(ReportRequestStatus.IN_PROGRESS).build();
        when(accessPolicy.currentUserId()).thenReturn(MANAGER);
        when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));
        when(accessPolicy.canManageRoom(MANAGER, ROOM)).thenReturn(true);
    }

    @Test
    void workRecordIsStoredWithoutAmountOrApproval() {
        when(recordRepository.save(any(ReportRecord.class))).thenAnswer(call -> call.getArgument(0));

        MaintenanceRecordResponse response = service.addRecord(request.getId(),
                new MaintenanceRecordRequest(ReportRecordRecordType.WORK, " Replaced leg ", null, "COMPLETED", null));

        assertThat(response.recordType()).isEqualTo(ReportRecordRecordType.WORK);
        assertThat(response.description()).isEqualTo("Replaced leg");
        assertThat(response.performedBy()).isEqualTo(MANAGER);
        assertThat(response.approvalStatus()).isNull();
        verify(auditActor).bind(MANAGER);
    }

    @Test
    void costRecordNeedsAmountAndStartsPending() {
        when(recordRepository.save(any(ReportRecord.class))).thenAnswer(call -> call.getArgument(0));

        MaintenanceRecordResponse response = service.addRecord(request.getId(),
                new MaintenanceRecordRequest(ReportRecordRecordType.COST, "Parts", new BigDecimal("100000"), null, null));

        assertThat(response.approvalStatus()).isEqualTo(ReportRecordApprovalStatus.PENDING);
        assertThat(response.amount()).isEqualByComparingTo("100000");
        assertError(() -> service.addRecord(request.getId(),
                new MaintenanceRecordRequest(ReportRecordRecordType.COST, "Parts", null, null, null)),
                ErrorCode.INVALID_REQUEST);
    }

    @Test
    void workRecordCannotCarryAmountAndOtherTypesAreRefused() {
        assertError(() -> service.addRecord(request.getId(),
                new MaintenanceRecordRequest(ReportRecordRecordType.WORK, "x", BigDecimal.TEN, null, null)),
                ErrorCode.INVALID_REQUEST);
        assertError(() -> service.addRecord(request.getId(),
                new MaintenanceRecordRequest(ReportRecordRecordType.LIABILITY, "x", null, null, null)),
                ErrorCode.INVALID_REQUEST);
        verify(recordRepository, never()).save(any());
    }

    @Test
    void rejectedRequestAcceptsNoRecords() {
        request.setStatus(ReportRequestStatus.REJECTED);

        assertError(() -> service.addRecord(request.getId(),
                new MaintenanceRecordRequest(ReportRecordRecordType.WORK, "x", null, null, null)),
                ErrorCode.MAINTENANCE_STATE_CONFLICT);
    }

    @Test
    void nonManagerCannotWriteRecords() {
        when(accessPolicy.canManageRoom(MANAGER, ROOM)).thenReturn(false);

        assertError(() -> service.addRecord(request.getId(),
                new MaintenanceRecordRequest(ReportRecordRecordType.WORK, "x", null, null, null)),
                ErrorCode.UNAUTHORIZED);
    }

    // ---- liabilities (P1-117)

    @Test
    void tenantLiabilityPointsAtTheRequestsContractTenant() {
        ReportRecord cost = cost(new BigDecimal("100000"), ReportRecordApprovalStatus.PENDING);
        stubRecord(cost);
        when(recordRepository.sumLiabilities(eq(cost.getId()), anyCollection())).thenReturn(new BigDecimal("40000"));
        when(recordRepository.save(any(ReportRecord.class))).thenAnswer(call -> call.getArgument(0));

        MaintenanceRecordResponse response = service.addLiability(request.getId(), cost.getId(),
                new LiabilityRequest(ReportRecordResponsibleParty.TENANT, new BigDecimal("60000"), "Tenant fault"));

        assertThat(response.recordType()).isEqualTo(ReportRecordRecordType.LIABILITY);
        assertThat(response.parentCostRecordId()).isEqualTo(cost.getId());
        assertThat(response.contractTenantId()).isEqualTo(TENANCY);
        assertThat(response.approvalStatus()).isEqualTo(ReportRecordApprovalStatus.PENDING);
    }

    @Test
    void ownerLiabilityHasNoContractTenant() {
        ReportRecord cost = cost(new BigDecimal("100000"), ReportRecordApprovalStatus.PENDING);
        stubRecord(cost);
        when(recordRepository.sumLiabilities(eq(cost.getId()), anyCollection())).thenReturn(BigDecimal.ZERO);
        when(recordRepository.save(any(ReportRecord.class))).thenAnswer(call -> call.getArgument(0));

        MaintenanceRecordResponse response = service.addLiability(request.getId(), cost.getId(),
                new LiabilityRequest(ReportRecordResponsibleParty.OWNER, new BigDecimal("40000"), "Wear"));

        assertThat(response.contractTenantId()).isNull();
        assertThat(response.responsibleParty()).isEqualTo(ReportRecordResponsibleParty.OWNER);
    }

    @Test
    void liabilitiesCannotExceedCost() {
        ReportRecord cost = cost(new BigDecimal("100000"), ReportRecordApprovalStatus.PENDING);
        stubRecord(cost);
        when(recordRepository.sumLiabilities(eq(cost.getId()), anyCollection())).thenReturn(new BigDecimal("70000"));

        assertError(() -> service.addLiability(request.getId(), cost.getId(),
                        new LiabilityRequest(ReportRecordResponsibleParty.TENANT, new BigDecimal("30001"), "x")),
                ErrorCode.MAINTENANCE_LIABILITY_EXCEEDS_COST);
        verify(recordRepository, never()).save(any());
    }

    @Test
    void liabilityNeedsPendingCostOfThisRequest() {
        ReportRecord approved = cost(BigDecimal.TEN, ReportRecordApprovalStatus.APPROVED);
        stubRecord(approved);
        assertError(() -> service.addLiability(request.getId(), approved.getId(),
                new LiabilityRequest(ReportRecordResponsibleParty.OWNER, BigDecimal.ONE, "x")),
                ErrorCode.MAINTENANCE_STATE_CONFLICT);

        UUID unknown = UUID.randomUUID();
        when(recordRepository.findByIdAndReportRequestId(unknown, request.getId())).thenReturn(Optional.empty());
        assertError(() -> service.addLiability(request.getId(), unknown,
                new LiabilityRequest(ReportRecordResponsibleParty.OWNER, BigDecimal.ONE, "x")),
                ErrorCode.REPORT_RECORD_NOT_FOUND);
    }

    @Test
    void tenantLiabilityNeedsRequestLinkedToATenancy() {
        request.setContractTenantId(null);
        ReportRecord cost = cost(BigDecimal.TEN, ReportRecordApprovalStatus.PENDING);
        stubRecord(cost);

        assertError(() -> service.addLiability(request.getId(), cost.getId(),
                new LiabilityRequest(ReportRecordResponsibleParty.TENANT, BigDecimal.ONE, "x")),
                ErrorCode.MAINTENANCE_STATE_CONFLICT);
    }

    // ---- approval (P1-118)

    @Test
    void costApprovalRequiresApprovedLiabilitiesToEqualTheCost() {
        ReportRecord cost = cost(new BigDecimal("100000"), ReportRecordApprovalStatus.PENDING);
        stubRecord(cost);
        when(recordRepository.sumLiabilities(eq(cost.getId()), anyCollection())).thenReturn(new BigDecimal("99999"));

        assertError(() -> service.approveRecord(request.getId(), cost.getId()),
                ErrorCode.MAINTENANCE_COST_LIABILITY_MISMATCH);
        verify(recordRepository, never()).decide(any(), any(), any(), any());
    }

    @Test
    void costIsApprovedWhenLiabilitiesMatch() {
        ReportRecord cost = cost(new BigDecimal("100000"), ReportRecordApprovalStatus.PENDING);
        stubRecord(cost);
        when(recordRepository.sumLiabilities(eq(cost.getId()), anyCollection())).thenReturn(new BigDecimal("100000"));
        when(recordRepository.decide(eq(cost.getId()), eq(ReportRecordApprovalStatus.APPROVED), eq(MANAGER),
                any(Instant.class))).thenReturn(1);

        service.approveRecord(request.getId(), cost.getId());

        verify(auditActor).bind(MANAGER);
    }

    @Test
    void liabilityOfAnApprovedCostIsFrozen() {
        ReportRecord cost = cost(BigDecimal.TEN, ReportRecordApprovalStatus.APPROVED);
        ReportRecord liability = liability(cost, ReportRecordApprovalStatus.PENDING);
        stubRecord(cost);
        stubRecord(liability);

        assertError(() -> service.approveRecord(request.getId(), liability.getId()),
                ErrorCode.MAINTENANCE_STATE_CONFLICT);
    }

    @Test
    void liabilityIsApprovedWhileCostIsPending() {
        ReportRecord cost = cost(BigDecimal.TEN, ReportRecordApprovalStatus.PENDING);
        ReportRecord liability = liability(cost, ReportRecordApprovalStatus.PENDING);
        stubRecord(cost);
        stubRecord(liability);
        when(recordRepository.decide(eq(liability.getId()), eq(ReportRecordApprovalStatus.APPROVED), eq(MANAGER),
                any(Instant.class))).thenReturn(1);

        service.approveRecord(request.getId(), liability.getId());

        verify(recordRepository).decide(eq(liability.getId()), eq(ReportRecordApprovalStatus.APPROVED), eq(MANAGER),
                any(Instant.class));
    }

    @Test
    void alreadyDecidedOrNonFinancialRecordsCannotBeDecided() {
        ReportRecord approved = cost(BigDecimal.TEN, ReportRecordApprovalStatus.APPROVED);
        stubRecord(approved);
        assertError(() -> service.rejectRecord(request.getId(), approved.getId()),
                ErrorCode.MAINTENANCE_STATE_CONFLICT);

        ReportRecord work = ReportRecord.builder().id(UUID.randomUUID()).reportRequestId(request.getId())
                .recordType(ReportRecordRecordType.WORK).description("x").performedBy(MANAGER).build();
        stubRecord(work);
        assertError(() -> service.approveRecord(request.getId(), work.getId()),
                ErrorCode.MAINTENANCE_STATE_CONFLICT);
    }

    @Test
    void racingDecisionLosesCompareAndSet() {
        ReportRecord cost = cost(BigDecimal.TEN, ReportRecordApprovalStatus.PENDING);
        stubRecord(cost);
        when(recordRepository.decide(any(), any(), any(), any())).thenReturn(0);

        assertError(() -> service.rejectRecord(request.getId(), cost.getId()), ErrorCode.MAINTENANCE_STATE_CONFLICT);
    }

    @Test
    void rejectionIsRecordedWithDecider() {
        ReportRecord cost = cost(BigDecimal.TEN, ReportRecordApprovalStatus.PENDING);
        stubRecord(cost);
        when(recordRepository.decide(eq(cost.getId()), eq(ReportRecordApprovalStatus.REJECTED), eq(MANAGER),
                any(Instant.class))).thenReturn(1);

        service.rejectRecord(request.getId(), cost.getId());

        ArgumentCaptor<ReportRecordApprovalStatus> decision = ArgumentCaptor.forClass(ReportRecordApprovalStatus.class);
        verify(recordRepository).decide(eq(cost.getId()), decision.capture(), eq(MANAGER), any(Instant.class));
        assertThat(decision.getValue()).isEqualTo(ReportRecordApprovalStatus.REJECTED);
    }

    private void stubRecord(ReportRecord record) {
        when(recordRepository.findByIdAndReportRequestId(record.getId(), request.getId()))
                .thenReturn(Optional.of(record));
    }

    private ReportRecord cost(BigDecimal amount, ReportRecordApprovalStatus status) {
        return ReportRecord.builder().id(UUID.randomUUID()).reportRequestId(request.getId())
                .recordType(ReportRecordRecordType.COST).amount(amount).description("cost").performedBy(MANAGER)
                .approvalStatus(status).build();
    }

    private ReportRecord liability(ReportRecord cost, ReportRecordApprovalStatus status) {
        return ReportRecord.builder().id(UUID.randomUUID()).reportRequestId(request.getId())
                .recordType(ReportRecordRecordType.LIABILITY).parentCostRecordId(cost.getId())
                .responsibleParty(ReportRecordResponsibleParty.OWNER).amount(BigDecimal.ONE).description("l")
                .performedBy(MANAGER).approvalStatus(status).build();
    }

    private static void assertError(org.assertj.core.api.ThrowableAssert.ThrowingCallable call, ErrorCode code) {
        assertThatThrownBy(call).isInstanceOf(AppException.class)
                .extracting(e -> ((AppException) e).getErrorCode()).isEqualTo(code);
    }
}
