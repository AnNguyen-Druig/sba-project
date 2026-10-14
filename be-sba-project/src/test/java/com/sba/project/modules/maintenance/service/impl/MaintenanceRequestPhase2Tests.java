package com.sba.project.modules.maintenance.service.impl;

import com.sba.project.common.audit.AuditActorContext;
import com.sba.project.common.exceptions.AppException;
import com.sba.project.common.exceptions.ErrorCode;
import com.sba.project.modules.maintenance.config.MaintenanceProperties;
import com.sba.project.modules.maintenance.dto.request.CreateMaintenanceRequest;
import com.sba.project.modules.maintenance.dto.request.RatingRequest;
import com.sba.project.modules.maintenance.dto.response.RepairCostStatResponse;
import com.sba.project.modules.maintenance.entity.Furniture;
import com.sba.project.modules.maintenance.entity.ReportRequest;
import com.sba.project.modules.maintenance.enums.FurnitureStatus;
import com.sba.project.modules.maintenance.enums.ReportRecordResponsibleParty;
import com.sba.project.modules.maintenance.enums.ReportRequestPriority;
import com.sba.project.modules.maintenance.enums.ReportRequestStatus;
import com.sba.project.modules.maintenance.mapper.MaintenanceRequestMapperImpl;
import com.sba.project.modules.maintenance.repository.FurnitureRepository;
import com.sba.project.modules.maintenance.repository.ReportRecordRepository;
import com.sba.project.modules.maintenance.repository.ReportRequestFurnitureRepository;
import com.sba.project.modules.maintenance.repository.ReportRequestRepository;
import com.sba.project.modules.maintenance.repository.ReportRequestServiceRepository;
import com.sba.project.modules.maintenance.service.MaintenanceAccessPolicy;
import com.sba.project.modules.maintenance.service.MaintenanceNotifier;
import com.sba.project.modules.maintenance.service.RoomTenancyPort;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Phase 2 additions to the request service: rating (P2-31), SLA (P2-30) and cost statistics (P2-32). */
@ExtendWith(MockitoExtension.class)
class MaintenanceRequestPhase2Tests {
    private static final UUID TENANT = UUID.randomUUID();
    private static final UUID MANAGER = UUID.randomUUID();
    private static final UUID ROOM = UUID.randomUUID();
    private static final UUID BRANCH = UUID.randomUUID();

    @Mock ReportRequestRepository requestRepository;
    @Mock ReportRequestFurnitureRepository furnitureLinkRepository;
    @Mock ReportRequestServiceRepository serviceLinkRepository;
    @Mock ReportRecordRepository recordRepository;
    @Mock FurnitureRepository furnitureRepository;
    @Mock MaintenanceAccessPolicy accessPolicy;
    @Mock RoomTenancyPort tenancyPort;
    @Mock MaintenanceNotifier notifier;
    @Mock AuditActorContext auditActor;
    final MaintenanceProperties properties = new MaintenanceProperties();
    MaintenanceRequestServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new MaintenanceRequestServiceImpl(requestRepository, furnitureLinkRepository, serviceLinkRepository,
                recordRepository, furnitureRepository, new MaintenanceRequestMapperImpl(),
                new MaintenanceRequestAccess(requestRepository, accessPolicy), accessPolicy, tenancyPort, notifier,
                auditActor, properties);
    }

    // ---- SLA (P2-30)

    @Test
    void configuredSlaSetsDeadlineOnlyForThatPriority() {
        properties.getSlaHours().put(ReportRequestPriority.URGENT, 24);
        UUID furnitureId = UUID.randomUUID();
        when(accessPolicy.currentUserId()).thenReturn(TENANT);
        when(tenancyPort.findActiveContractTenantId(TENANT, ROOM)).thenReturn(Optional.of(UUID.randomUUID()));
        when(furnitureRepository.findAllById(any())).thenReturn(List.of(
                Furniture.builder().id(furnitureId).roomId(ROOM).assetCode("A").name("n")
                        .status(FurnitureStatus.IN_USE).build()));
        when(requestRepository.save(any(ReportRequest.class))).thenAnswer(call -> {
            ReportRequest saved = call.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        service.createRequest(create(ReportRequestPriority.URGENT, furnitureId));
        service.createRequest(create(ReportRequestPriority.LOW, furnitureId));

        ArgumentCaptor<ReportRequest> saved = ArgumentCaptor.forClass(ReportRequest.class);
        verify(requestRepository, org.mockito.Mockito.times(2)).save(saved.capture());
        Instant urgentDeadline = saved.getAllValues().get(0).getSlaDueAt();
        assertThat(urgentDeadline).isBetween(Instant.now().plusSeconds(24 * 3600 - 60),
                Instant.now().plusSeconds(24 * 3600 + 60));
        assertThat(saved.getAllValues().get(1).getSlaDueAt()).isNull();
    }

    @Test
    void overdueBacklogIsAManagerOnlyRoomQuery() {
        when(accessPolicy.currentUserId()).thenReturn(MANAGER);
        when(accessPolicy.canManageRoom(MANAGER, ROOM)).thenReturn(true);
        when(requestRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenAnswer(call -> new PageImpl<>(List.of(), call.getArgument(1), 0));

        assertThat(service.searchRoomRequests(ROOM, null, null, true, PageRequest.of(0, 10)).getContent()).isEmpty();
        verify(requestRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    // ---- rating (P2-31)

    @Test
    void requesterRatesACompletedRequestOnce() {
        ReportRequest request = request(ReportRequestStatus.COMPLETED, null);
        when(accessPolicy.currentUserId()).thenReturn(TENANT);
        when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));
        when(requestRepository.rate(eq(request.getId()), eq(TENANT), eq(5), eq("Fast"), any(Instant.class)))
                .thenReturn(1);

        service.rate(request.getId(), new RatingRequest(5, " Fast "));

        verify(requestRepository).rate(eq(request.getId()), eq(TENANT), eq(5), eq("Fast"), any(Instant.class));
    }

    @Test
    void ratingRejectsOthersUnfinishedAndAlreadyRatedRequests() {
        ReportRequest done = request(ReportRequestStatus.COMPLETED, null);
        when(accessPolicy.currentUserId()).thenReturn(UUID.randomUUID());
        when(requestRepository.findById(done.getId())).thenReturn(Optional.of(done));
        assertError(() -> service.rate(done.getId(), new RatingRequest(4, null)), ErrorCode.UNAUTHORIZED);

        ReportRequest open = request(ReportRequestStatus.IN_PROGRESS, null);
        when(accessPolicy.currentUserId()).thenReturn(TENANT);
        when(requestRepository.findById(open.getId())).thenReturn(Optional.of(open));
        assertError(() -> service.rate(open.getId(), new RatingRequest(4, null)), ErrorCode.MAINTENANCE_STATE_CONFLICT);

        ReportRequest rated = request(ReportRequestStatus.COMPLETED, 3);
        when(requestRepository.findById(rated.getId())).thenReturn(Optional.of(rated));
        assertError(() -> service.rate(rated.getId(), new RatingRequest(4, null)), ErrorCode.MAINTENANCE_STATE_CONFLICT);
        verify(requestRepository, never()).rate(any(), any(), anyInt(), any(), any());
    }

    @Test
    void racingSecondRatingLosesCompareAndSet() {
        ReportRequest request = request(ReportRequestStatus.COMPLETED, null);
        when(accessPolicy.currentUserId()).thenReturn(TENANT);
        when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));
        when(requestRepository.rate(any(), any(), anyInt(), any(), any())).thenReturn(0);

        assertError(() -> service.rate(request.getId(), new RatingRequest(2, null)),
                ErrorCode.MAINTENANCE_STATE_CONFLICT);
    }

    // ---- cost statistics (P2-32)

    @Test
    void costStatsByRoomAndByBranchUseTheMatchingScope() {
        List<RepairCostStatResponse> stats = List.of(
                new RepairCostStatResponse(ROOM, ReportRecordResponsibleParty.TENANT, new BigDecimal("60000"), 1));
        when(accessPolicy.currentUserId()).thenReturn(MANAGER);
        when(accessPolicy.canManageRoom(MANAGER, ROOM)).thenReturn(true);
        when(accessPolicy.canManageBranch(MANAGER, BRANCH)).thenReturn(true);
        when(recordRepository.costStatsByRoom(ROOM)).thenReturn(stats);
        when(recordRepository.costStatsByBranch(BRANCH)).thenReturn(stats);

        assertThat(service.costStats(ROOM, null)).isEqualTo(stats);
        assertThat(service.costStats(null, BRANCH)).isEqualTo(stats);
    }

    @Test
    void costStatsNeedExactlyOneScopeAndPermission() {
        when(accessPolicy.currentUserId()).thenReturn(MANAGER);
        assertError(() -> service.costStats(null, null), ErrorCode.INVALID_REQUEST);
        assertError(() -> service.costStats(ROOM, BRANCH), ErrorCode.INVALID_REQUEST);

        when(accessPolicy.canManageRoom(MANAGER, ROOM)).thenReturn(false);
        assertError(() -> service.costStats(ROOM, null), ErrorCode.UNAUTHORIZED);
        when(accessPolicy.canManageBranch(MANAGER, BRANCH)).thenReturn(false);
        assertError(() -> service.costStats(null, BRANCH), ErrorCode.UNAUTHORIZED);
        verify(recordRepository, never()).costStatsByRoom(any());
        verify(recordRepository, never()).costStatsByBranch(any());
    }

    private static CreateMaintenanceRequest create(ReportRequestPriority priority, UUID furnitureId) {
        return new CreateMaintenanceRequest(ROOM, "Title", "Description", priority,
                List.of(new CreateMaintenanceRequest.FurnitureItem(furnitureId, null)), null, null);
    }

    private static ReportRequest request(ReportRequestStatus status, Integer rating) {
        return ReportRequest.builder().id(UUID.randomUUID()).roomId(ROOM).requesterUserId(TENANT).title("t")
                .description("d").priority(ReportRequestPriority.NORMAL).status(status).rating(rating).build();
    }

    private static void assertError(org.assertj.core.api.ThrowableAssert.ThrowingCallable call, ErrorCode code) {
        assertThatThrownBy(call).isInstanceOf(AppException.class)
                .extracting(e -> ((AppException) e).getErrorCode()).isEqualTo(code);
    }
}
