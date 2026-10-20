package com.sba.project.modules.maintenance.service.impl;

import com.sba.project.common.audit.AuditActorContext;
import com.sba.project.common.base.PagedResponse;
import com.sba.project.common.exceptions.AppException;
import com.sba.project.common.exceptions.ErrorCode;
import com.sba.project.modules.maintenance.config.MaintenanceProperties;
import com.sba.project.modules.maintenance.dto.request.AssignRequest;
import com.sba.project.modules.maintenance.dto.request.CreateMaintenanceRequest;
import com.sba.project.modules.maintenance.dto.request.EvidenceRequest;
import com.sba.project.modules.maintenance.dto.request.TransitionRequest;
import com.sba.project.modules.maintenance.dto.response.MaintenanceRequestDetailResponse;
import com.sba.project.modules.maintenance.dto.response.MaintenanceRequestSummaryResponse;
import com.sba.project.modules.maintenance.entity.Furniture;
import com.sba.project.modules.maintenance.entity.ReportRecord;
import com.sba.project.modules.maintenance.entity.ReportRequest;
import com.sba.project.modules.maintenance.enums.EvidenceStage;
import com.sba.project.modules.maintenance.enums.FurnitureStatus;
import com.sba.project.modules.maintenance.enums.ReportRecordRecordType;
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
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** Policy, tenancy and notifier are Mockito doubles for the Student 1/3/4 integration points. */
@ExtendWith(MockitoExtension.class)
class MaintenanceRequestServiceImplTests {
    private static final UUID TENANT = UUID.randomUUID();
    private static final UUID MANAGER = UUID.randomUUID();
    private static final UUID ROOM = UUID.randomUUID();
    private static final UUID TENANCY = UUID.randomUUID();

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

    // ---- create (P1-111 / P1-112)

    @Test
    void tenantCreatesOpenRequestForFurnitureInOwnRoom() {
        UUID furnitureId = UUID.randomUUID();
        asTenant();
        when(tenancyPort.findActiveContractTenantId(TENANT, ROOM)).thenReturn(Optional.of(TENANCY));
        when(furnitureRepository.findAllById(List.of(furnitureId))).thenReturn(List.of(furniture(furnitureId, ROOM)));
        when(requestRepository.save(any(ReportRequest.class))).thenAnswer(call -> {
            ReportRequest saved = call.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        MaintenanceRequestDetailResponse response = service.createRequest(new CreateMaintenanceRequest(ROOM,
                " Broken desk ", "Leg is loose", ReportRequestPriority.HIGH,
                List.of(new CreateMaintenanceRequest.FurnitureItem(furnitureId, "loose leg")), null,
                List.of(new EvidenceRequest("img/1", "image", EvidenceStage.BEFORE))));

        ArgumentCaptor<ReportRequest> saved = ArgumentCaptor.forClass(ReportRequest.class);
        verify(requestRepository).save(saved.capture());
        assertThat(saved.getValue().getStatus()).isEqualTo(ReportRequestStatus.OPEN);
        assertThat(saved.getValue().getRequesterUserId()).isEqualTo(TENANT);
        assertThat(saved.getValue().getContractTenantId()).isEqualTo(TENANCY);
        assertThat(saved.getValue().getTitle()).isEqualTo("Broken desk");
        assertThat(saved.getValue().getEvidenceRefs().get(0).get("stage").asString()).isEqualTo("BEFORE");
        assertThat(response.status()).isEqualTo(ReportRequestStatus.OPEN);
        verify(furnitureLinkRepository).saveAll(any());
    }

    @Test
    void createRequiresAtLeastOneItem() {
        asTenant();

        assertError(() -> service.createRequest(create(null, null)), ErrorCode.MAINTENANCE_ITEM_REQUIRED);
        verify(requestRepository, never()).save(any());
    }

    @Test
    void createIsRejectedWhenCallerDoesNotRentTheRoom() {
        asTenant();
        when(tenancyPort.findActiveContractTenantId(TENANT, ROOM)).thenReturn(Optional.empty());

        assertError(() -> service.createRequest(create(List.of(furnitureItem(UUID.randomUUID())), null)),
                ErrorCode.UNAUTHORIZED);
        verify(requestRepository, never()).save(any());
    }

    @Test
    void createRejectsFurnitureOfAnotherRoom() {
        UUID furnitureId = UUID.randomUUID();
        asTenant();
        when(tenancyPort.findActiveContractTenantId(TENANT, ROOM)).thenReturn(Optional.of(TENANCY));
        when(furnitureRepository.findAllById(List.of(furnitureId)))
                .thenReturn(List.of(furniture(furnitureId, UUID.randomUUID())));

        assertError(() -> service.createRequest(create(List.of(furnitureItem(furnitureId)), null)),
                ErrorCode.MAINTENANCE_ITEM_ROOM_MISMATCH);
        verify(requestRepository, never()).save(any());
    }

    @Test
    void createRejectsUnknownFurniture() {
        UUID furnitureId = UUID.randomUUID();
        asTenant();
        when(tenancyPort.findActiveContractTenantId(TENANT, ROOM)).thenReturn(Optional.of(TENANCY));
        when(furnitureRepository.findAllById(List.of(furnitureId))).thenReturn(List.of());

        assertError(() -> service.createRequest(create(List.of(furnitureItem(furnitureId)), null)),
                ErrorCode.FURNITURE_NOT_FOUND);
    }

    @Test
    void createRejectsDuplicateItems() {
        UUID furnitureId = UUID.randomUUID();
        asTenant();
        when(tenancyPort.findActiveContractTenantId(TENANT, ROOM)).thenReturn(Optional.of(TENANCY));

        assertError(() -> service.createRequest(create(List.of(furnitureItem(furnitureId),
                furnitureItem(furnitureId)), null)), ErrorCode.MAINTENANCE_DUPLICATE_ITEM);

        UUID roomServiceId = UUID.randomUUID();
        assertError(() -> service.createRequest(create(null, List.of(
                new CreateMaintenanceRequest.ServiceItem(roomServiceId, null),
                new CreateMaintenanceRequest.ServiceItem(roomServiceId, null)))),
                ErrorCode.MAINTENANCE_DUPLICATE_ITEM);
    }

    // ---- status actions (P1-114 / P1-119)

    @Test
    void managerAcceptsOpenRequestRecordsStatusAndNotifiesTenant() {
        ReportRequest request = request(ReportRequestStatus.OPEN);
        asManager(request);
        when(requestRepository.transition(eq(request.getId()), eq(ReportRequestStatus.OPEN),
                eq(ReportRequestStatus.ACCEPTED), eq(4L), any(Instant.class))).thenReturn(1);

        service.accept(request.getId(), new TransitionRequest(" On our way "));

        ArgumentCaptor<ReportRecord> record = ArgumentCaptor.forClass(ReportRecord.class);
        verify(recordRepository).save(record.capture());
        assertThat(record.getValue().getRecordType()).isEqualTo(ReportRecordRecordType.STATUS);
        assertThat(record.getValue().getStatusAfter()).isEqualTo("ACCEPTED");
        assertThat(record.getValue().getDescription()).isEqualTo("On our way");
        assertThat(record.getValue().getPerformedBy()).isEqualTo(MANAGER);
        verify(auditActor).bind(MANAGER);
        verify(notifier).notifyUser(eq(TENANT), eq("REPAIR_REQUEST_ACCEPTED"), any(), any(), eq(request.getId()));
    }

    @Test
    void completeStampsCompletionThroughDedicatedUpdate() {
        ReportRequest request = request(ReportRequestStatus.IN_PROGRESS);
        asManager(request);
        when(requestRepository.complete(eq(request.getId()), eq(ReportRequestStatus.IN_PROGRESS),
                eq(ReportRequestStatus.COMPLETED), eq(4L), any(Instant.class))).thenReturn(1);

        service.complete(request.getId(), null);

        verify(notifier).notifyUser(eq(TENANT), eq("REPAIR_REQUEST_COMPLETED"), any(), any(), eq(request.getId()));
        verify(requestRepository, never()).transition(any(), any(), any(), anyLong(), any());
    }

    @Test
    void actionsFollowOnlyTheDocumentedVerbSequence() {
        ReportRequest request = request(ReportRequestStatus.OPEN);
        asManager(request);

        assertError(() -> service.start(request.getId(), null), ErrorCode.MAINTENANCE_STATE_CONFLICT);
        assertError(() -> service.complete(request.getId(), null), ErrorCode.MAINTENANCE_STATE_CONFLICT);
        verifyNoInteractions(notifier);
        verify(recordRepository, never()).save(any());
    }

    @Test
    void terminalRequestsCannotBeAcceptedOrRejected() {
        ReportRequest request = request(ReportRequestStatus.REJECTED);
        asManager(request);

        assertError(() -> service.accept(request.getId(), null), ErrorCode.MAINTENANCE_STATE_CONFLICT);
        assertError(() -> service.reject(request.getId(), null), ErrorCode.MAINTENANCE_STATE_CONFLICT);
    }

    @Test
    void lostCompareAndSetIsReportedAsConcurrentModification() {
        ReportRequest request = request(ReportRequestStatus.OPEN);
        asManager(request);
        when(requestRepository.transition(any(), any(), any(), anyLong(), any())).thenReturn(0);

        assertError(() -> service.reject(request.getId(), null), ErrorCode.CONCURRENT_MODIFICATION);
        verify(recordRepository, never()).save(any());
        verifyNoInteractions(notifier);
    }

    @Test
    void requestingTenantCannotRunManagerActions() {
        ReportRequest request = request(ReportRequestStatus.OPEN);
        when(accessPolicy.currentUserId()).thenReturn(TENANT);
        when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));
        when(accessPolicy.canManageRoom(TENANT, ROOM)).thenReturn(false);

        assertError(() -> service.accept(request.getId(), null), ErrorCode.UNAUTHORIZED);
        verify(requestRepository, never()).transition(any(), any(), any(), anyLong(), any());
    }

    @Test
    void unknownRequestIsNotFound() {
        UUID id = UUID.randomUUID();
        when(accessPolicy.currentUserId()).thenReturn(MANAGER);
        when(requestRepository.findById(id)).thenReturn(Optional.empty());

        assertError(() -> service.accept(id, null), ErrorCode.REPORT_REQUEST_NOT_FOUND);
    }

    // ---- assignment (P1-115)

    @Test
    void assignSetsSingleAssigneeAndWritesAssignmentRecord() {
        ReportRequest request = request(ReportRequestStatus.ACCEPTED);
        UUID assignee = UUID.randomUUID();
        asManager(request);
        when(accessPolicy.canManageRoom(assignee, ROOM)).thenReturn(true);
        when(requestRepository.assign(eq(request.getId()), eq(assignee), eq(4L), anyCollection(), any(Instant.class)))
                .thenReturn(1);

        service.assign(request.getId(), new AssignRequest(assignee));

        ArgumentCaptor<ReportRecord> record = ArgumentCaptor.forClass(ReportRecord.class);
        verify(recordRepository).save(record.capture());
        assertThat(record.getValue().getRecordType()).isEqualTo(ReportRecordRecordType.ASSIGNMENT);
        assertThat(record.getValue().getAssignedTo()).isEqualTo(assignee);
    }

    @Test
    void assigneeMustBeAbleToHandleTheRoom() {
        ReportRequest request = request(ReportRequestStatus.ACCEPTED);
        UUID stranger = UUID.randomUUID();
        asManager(request);
        when(accessPolicy.canManageRoom(stranger, ROOM)).thenReturn(false);

        assertError(() -> service.assign(request.getId(), new AssignRequest(stranger)),
                ErrorCode.MAINTENANCE_INVALID_ASSIGNEE);
        verify(requestRepository, never()).assign(any(), any(), anyLong(), anyCollection(), any());
    }

    @Test
    void closedRequestCannotBeReassigned() {
        ReportRequest request = request(ReportRequestStatus.COMPLETED);
        asManager(request);

        assertError(() -> service.assign(request.getId(), new AssignRequest(UUID.randomUUID())),
                ErrorCode.MAINTENANCE_STATE_CONFLICT);
    }

    @Test
    void staleAssignmentLosesCompareAndSet() {
        ReportRequest request = request(ReportRequestStatus.OPEN);
        UUID assignee = UUID.randomUUID();
        asManager(request);
        when(accessPolicy.canManageRoom(assignee, ROOM)).thenReturn(true);
        when(requestRepository.assign(any(), any(), anyLong(), anyCollection(), any())).thenReturn(0);

        assertError(() -> service.assign(request.getId(), new AssignRequest(assignee)),
                ErrorCode.CONCURRENT_MODIFICATION);
        verify(recordRepository, never()).save(any());
    }

    // ---- evidence (P1-113)

    @Test
    void requesterAppendsEvidenceToOwnOpenRequest() {
        ReportRequest request = request(ReportRequestStatus.OPEN);
        asTenantOf(request);
        when(requestRepository.appendEvidence(eq(request.getId()), any())).thenReturn(1);

        service.addEvidence(request.getId(), new EvidenceRequest("img/2", "image", EvidenceStage.AFTER));

        ArgumentCaptor<String> json = ArgumentCaptor.forClass(String.class);
        verify(requestRepository).appendEvidence(eq(request.getId()), json.capture());
        assertThat(json.getValue()).contains("\"public_id\":\"img/2\"").contains("\"stage\":\"AFTER\"")
                .contains(TENANT.toString());
    }

    @Test
    void evidenceIsRejectedForOutsidersAndClosedRequests() {
        ReportRequest request = request(ReportRequestStatus.OPEN);
        UUID outsider = UUID.randomUUID();
        when(accessPolicy.currentUserId()).thenReturn(outsider);
        when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));
        when(accessPolicy.canManageRoom(outsider, ROOM)).thenReturn(false);
        assertError(() -> service.addEvidence(request.getId(), new EvidenceRequest("i", "image", EvidenceStage.AFTER)),
                ErrorCode.UNAUTHORIZED);

        ReportRequest rejected = request(ReportRequestStatus.REJECTED);
        asTenantOf(rejected);
        assertError(() -> service.addEvidence(rejected.getId(), new EvidenceRequest("i", "image", EvidenceStage.AFTER)),
                ErrorCode.MAINTENANCE_STATE_CONFLICT);
        verify(requestRepository, never()).appendEvidence(any(), any());
    }

    // ---- reads (P1-114 / P1-119)

    @Test
    void tenantDetailHidesCostAndLiabilityRecordsButManagerSeesThem() {
        ReportRequest request = request(ReportRequestStatus.COMPLETED);
        when(recordRepository.findByReportRequestIdOrderByOccurredAtAsc(request.getId())).thenReturn(List.of(
                record(request, ReportRecordRecordType.WORK), record(request, ReportRecordRecordType.COST),
                record(request, ReportRecordRecordType.LIABILITY), record(request, ReportRecordRecordType.STATUS)));

        asTenantOf(request);
        assertThat(service.getRequest(request.getId()).records()).extracting(r -> r.recordType())
                .containsExactly(ReportRecordRecordType.WORK, ReportRecordRecordType.STATUS);

        asManager(request);
        assertThat(service.getRequest(request.getId()).records()).hasSize(4);
    }

    @Test
    void outsiderCannotReadRequest() {
        ReportRequest request = request(ReportRequestStatus.OPEN);
        UUID outsider = UUID.randomUUID();
        when(accessPolicy.currentUserId()).thenReturn(outsider);
        when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));
        when(accessPolicy.canManageRoom(outsider, ROOM)).thenReturn(false);

        assertError(() -> service.getRequest(request.getId()), ErrorCode.UNAUTHORIZED);
    }

    @Test
    void myRequestsAreScopedToCallerWithNewestFirstDefault() {
        when(accessPolicy.currentUserId()).thenReturn(TENANT);
        ReportRequest request = request(ReportRequestStatus.OPEN);
        when(requestRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenAnswer(call -> new PageImpl<>(List.of(request), call.getArgument(1), 1));

        PagedResponse<MaintenanceRequestSummaryResponse> page = service.listMyRequests(null, PageRequest.of(0, 10));

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(requestRepository).findAll(any(Specification.class), pageable.capture());
        assertThat(pageable.getValue().getSort()).isEqualTo(Sort.by(Sort.Direction.DESC, "createdAt"));
        assertThat(page.getNumber()).isEqualTo(1);
        assertThat(page.getContent()).extracting(MaintenanceRequestSummaryResponse::id).containsExactly(request.getId());
    }

    @Test
    void roomSearchNeedsBranchScopeAndAllowedSort() {
        when(accessPolicy.currentUserId()).thenReturn(MANAGER);
        when(accessPolicy.canManageRoom(MANAGER, ROOM)).thenReturn(false);
        assertError(() -> service.searchRoomRequests(ROOM, null, null, false, PageRequest.of(0, 10)), ErrorCode.UNAUTHORIZED);

        when(accessPolicy.canManageRoom(MANAGER, ROOM)).thenReturn(true);
        assertError(() -> service.searchRoomRequests(ROOM, null, null, false, PageRequest.of(0, 10, Sort.by("roomId"))),
                ErrorCode.INVALID_REQUEST);
        verify(requestRepository, never()).findAll(any(Specification.class), any(Pageable.class));
    }

    // ---- helpers

    private void asTenant() {
        when(accessPolicy.currentUserId()).thenReturn(TENANT);
    }

    private void asTenantOf(ReportRequest request) {
        when(accessPolicy.currentUserId()).thenReturn(TENANT);
        when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));
        when(accessPolicy.canManageRoom(TENANT, ROOM)).thenReturn(false);
    }

    private void asManager(ReportRequest request) {
        when(accessPolicy.currentUserId()).thenReturn(MANAGER);
        when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));
        when(accessPolicy.canManageRoom(MANAGER, ROOM)).thenReturn(true);
    }

    private static ReportRequest request(ReportRequestStatus status) {
        return ReportRequest.builder().id(UUID.randomUUID()).roomId(ROOM).requesterUserId(TENANT)
                .contractTenantId(TENANCY).title("Broken desk").description("Leg is loose")
                .priority(ReportRequestPriority.NORMAL).status(status).version(4L).build();
    }

    private static ReportRecord record(ReportRequest request, ReportRecordRecordType type) {
        return ReportRecord.builder().id(UUID.randomUUID()).reportRequestId(request.getId()).recordType(type)
                .description("x").performedBy(MANAGER).build();
    }

    private static Furniture furniture(UUID id, UUID roomId) {
        return Furniture.builder().id(id).roomId(roomId).assetCode("D-1").name("Desk")
                .status(FurnitureStatus.IN_USE).build();
    }

    private static CreateMaintenanceRequest.FurnitureItem furnitureItem(UUID id) {
        return new CreateMaintenanceRequest.FurnitureItem(id, null);
    }

    private static CreateMaintenanceRequest create(List<CreateMaintenanceRequest.FurnitureItem> furniture,
            List<CreateMaintenanceRequest.ServiceItem> services) {
        return new CreateMaintenanceRequest(ROOM, "Title", "Description", ReportRequestPriority.NORMAL,
                furniture, services, null);
    }

    private static void assertError(org.assertj.core.api.ThrowableAssert.ThrowingCallable call, ErrorCode code) {
        assertThatThrownBy(call).isInstanceOf(AppException.class)
                .extracting(e -> ((AppException) e).getErrorCode()).isEqualTo(code);
    }
}
