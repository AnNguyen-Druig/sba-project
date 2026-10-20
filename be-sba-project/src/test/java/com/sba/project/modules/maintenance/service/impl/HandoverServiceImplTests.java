package com.sba.project.modules.maintenance.service.impl;

import com.sba.project.common.audit.AuditActorContext;
import com.sba.project.common.exceptions.AppException;
import com.sba.project.common.exceptions.ErrorCode;
import com.sba.project.modules.leasing.entity.ContractTenant;
import com.sba.project.modules.leasing.enums.ContractTenantStatus;
import com.sba.project.modules.leasing.repository.ContractTenantHandoverRepository;
import com.sba.project.modules.leasing.repository.TenantDirectoryRepository;
import com.sba.project.modules.maintenance.dto.request.HandoverRequest;
import com.sba.project.modules.maintenance.dto.response.HandoverResponse;
import com.sba.project.modules.maintenance.entity.Furniture;
import com.sba.project.modules.maintenance.enums.FurnitureStatus;
import com.sba.project.modules.maintenance.repository.FurnitureRepository;
import com.sba.project.modules.maintenance.service.MaintenanceAccessPolicy;
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
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HandoverServiceImplTests {
    private static final UUID MANAGER = UUID.randomUUID();
    private static final UUID TENANT = UUID.randomUUID();
    private static final UUID ROOM = UUID.randomUUID();
    private static final UUID MEMBER = UUID.randomUUID();

    @Mock TenantDirectoryRepository tenantRepository;
    @Mock ContractTenantHandoverRepository handoverRepository;
    @Mock FurnitureRepository furnitureRepository;
    @Mock MaintenanceAccessPolicy accessPolicy;
    @Mock AuditActorContext auditActor;
    HandoverServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new HandoverServiceImpl(tenantRepository, handoverRepository, furnitureRepository, accessPolicy,
                auditActor);
    }

    @Test
    void managerRecordsServerDerivedSnapshotOnce() {
        UUID furnitureId = UUID.randomUUID();
        ContractTenant before = member(ContractTenantStatus.ACTIVE, null);
        ContractTenant after = member(ContractTenantStatus.ACTIVE, Instant.now());
        after.setHandoverBy(MANAGER);
        after.setHandoverSnapshot(JsonMapper.builder().build().createObjectNode());
        when(accessPolicy.currentUserId()).thenReturn(MANAGER);
        when(accessPolicy.canManageRoom(MANAGER, ROOM)).thenReturn(true);
        when(tenantRepository.findById(MEMBER)).thenReturn(Optional.of(before), Optional.of(after));
        when(furnitureRepository.findAllById(List.of(furnitureId))).thenReturn(List.of(
                Furniture.builder().id(furnitureId).roomId(ROOM).assetCode("DESK-1").name("Desk")
                        .status(FurnitureStatus.IN_USE).build()));
        when(handoverRepository.record(eq(MEMBER), eq(MANAGER), any(JsonNode.class), any(JsonNode.class),
                anyCollection(), any(Instant.class))).thenReturn(1);

        HandoverResponse response = service.recordHandover(MEMBER, new HandoverRequest(
                List.of(new HandoverRequest.Item(furnitureId, " Scratched ", "left corner")), null));

        ArgumentCaptor<JsonNode> snapshot = ArgumentCaptor.forClass(JsonNode.class);
        verify(handoverRepository).record(eq(MEMBER), eq(MANAGER), snapshot.capture(), any(JsonNode.class),
                anyCollection(), any(Instant.class));
        JsonNode item = snapshot.getValue().get("items").get(0);
        assertThat(item.get("asset_code").asString()).isEqualTo("DESK-1");
        assertThat(item.get("furniture_status").asString()).isEqualTo("IN_USE");
        assertThat(item.get("condition").asString()).isEqualTo("Scratched");
        assertThat(item.get("note").asString()).isEqualTo("left corner");
        verify(auditActor).bind(MANAGER);
        assertThat(response.handoverBy()).isEqualTo(MANAGER);
    }

    @Test
    void handoverIsRefusedOutsideScopeOrWhenAlreadyRecordedOrWrongStatus() {
        when(accessPolicy.currentUserId()).thenReturn(MANAGER);
        when(accessPolicy.canManageRoom(MANAGER, ROOM)).thenReturn(false);
        when(tenantRepository.findById(MEMBER)).thenReturn(Optional.of(member(ContractTenantStatus.ACTIVE, null)));
        HandoverRequest request = new HandoverRequest(
                List.of(new HandoverRequest.Item(UUID.randomUUID(), "ok", null)), null);
        assertError(() -> service.recordHandover(MEMBER, request), ErrorCode.UNAUTHORIZED);

        when(accessPolicy.canManageRoom(MANAGER, ROOM)).thenReturn(true);
        when(tenantRepository.findById(MEMBER)).thenReturn(Optional.of(member(ContractTenantStatus.ACTIVE,
                Instant.now())));
        assertError(() -> service.recordHandover(MEMBER, request), ErrorCode.MAINTENANCE_STATE_CONFLICT);

        when(tenantRepository.findById(MEMBER)).thenReturn(Optional.of(member(ContractTenantStatus.DRAFT, null)));
        assertError(() -> service.recordHandover(MEMBER, request), ErrorCode.MAINTENANCE_STATE_CONFLICT);
        verify(handoverRepository, never()).record(any(), any(), any(), any(), any(), any());
    }

    @Test
    void itemsMustBeDistinctExistingAndInTheMembersRoom() {
        UUID furnitureId = UUID.randomUUID();
        when(accessPolicy.currentUserId()).thenReturn(MANAGER);
        when(accessPolicy.canManageRoom(MANAGER, ROOM)).thenReturn(true);
        when(tenantRepository.findById(MEMBER)).thenReturn(Optional.of(member(ContractTenantStatus.ACTIVE, null)));

        assertError(() -> service.recordHandover(MEMBER, new HandoverRequest(List.of(
                new HandoverRequest.Item(furnitureId, "a", null),
                new HandoverRequest.Item(furnitureId, "b", null)), null)), ErrorCode.MAINTENANCE_DUPLICATE_ITEM);

        when(furnitureRepository.findAllById(List.of(furnitureId))).thenReturn(List.of());
        assertError(() -> service.recordHandover(MEMBER, new HandoverRequest(
                List.of(new HandoverRequest.Item(furnitureId, "a", null)), null)), ErrorCode.FURNITURE_NOT_FOUND);

        when(furnitureRepository.findAllById(List.of(furnitureId))).thenReturn(List.of(
                Furniture.builder().id(furnitureId).roomId(UUID.randomUUID()).assetCode("X").name("x")
                        .status(FurnitureStatus.IN_USE).build()));
        assertError(() -> service.recordHandover(MEMBER, new HandoverRequest(
                List.of(new HandoverRequest.Item(furnitureId, "a", null)), null)),
                ErrorCode.MAINTENANCE_ITEM_ROOM_MISMATCH);
    }

    @Test
    void memberAndRoomManagerCanReadButStrangersCannot() {
        ContractTenant recorded = member(ContractTenantStatus.LEFT, Instant.now());
        when(tenantRepository.findById(MEMBER)).thenReturn(Optional.of(recorded));

        when(accessPolicy.currentUserId()).thenReturn(TENANT);
        assertThat(service.getHandover(MEMBER).contractTenantId()).isEqualTo(MEMBER);

        UUID stranger = UUID.randomUUID();
        when(accessPolicy.currentUserId()).thenReturn(stranger);
        when(accessPolicy.canManageRoom(stranger, ROOM)).thenReturn(false);
        assertError(() -> service.getHandover(MEMBER), ErrorCode.UNAUTHORIZED);
    }

    @Test
    void readingBeforeAnyHandoverIsNotFound() {
        when(accessPolicy.currentUserId()).thenReturn(TENANT);
        when(tenantRepository.findById(MEMBER)).thenReturn(Optional.of(member(ContractTenantStatus.ACTIVE, null)));

        assertError(() -> service.getHandover(MEMBER), ErrorCode.RESOURCE_NOT_FOUND);
    }

    private static ContractTenant member(ContractTenantStatus status, Instant handoverAt) {
        return ContractTenant.builder().id(MEMBER).tenantUserId(TENANT).roomId(ROOM).status(status)
                .handoverAt(handoverAt).build();
    }

    private static void assertError(org.assertj.core.api.ThrowableAssert.ThrowingCallable call, ErrorCode code) {
        assertThatThrownBy(call).isInstanceOf(AppException.class)
                .extracting(e -> ((AppException) e).getErrorCode()).isEqualTo(code);
    }
}
