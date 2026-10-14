package com.sba.project.modules.leasing.service.impl;

import com.sba.project.common.exceptions.AppException;
import com.sba.project.common.exceptions.ErrorCode;
import com.sba.project.modules.leasing.dto.response.OccupancyResponse;
import com.sba.project.modules.leasing.dto.response.TenantResponse;
import com.sba.project.modules.leasing.entity.ContractTenant;
import com.sba.project.modules.leasing.entity.RoomSlotOccupancy;
import com.sba.project.modules.leasing.enums.ContractTenantRole;
import com.sba.project.modules.leasing.enums.ContractTenantStatus;
import com.sba.project.modules.leasing.mapper.TenantMapperImpl;
import com.sba.project.modules.leasing.repository.OccupancyReadRepository;
import com.sba.project.modules.leasing.repository.TenantDirectoryRepository;
import com.sba.project.modules.leasing.service.TenantAccessPolicy;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TenantDirectoryServiceImplTests {
    private static final UUID ACTOR = UUID.randomUUID();
    private static final UUID BRANCH = UUID.randomUUID();
    private static final UUID ROOM = UUID.randomUUID();
    private static final UUID CONTRACT = UUID.randomUUID();

    @Mock TenantDirectoryRepository tenantRepository;
    @Mock OccupancyReadRepository occupancyRepository;
    @Mock TenantAccessPolicy accessPolicy;
    TenantDirectoryServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new TenantDirectoryServiceImpl(tenantRepository, occupancyRepository, new TenantMapperImpl(),
                accessPolicy);
        when(accessPolicy.currentUserId()).thenReturn(ACTOR);
    }

    @Test
    void branchLookupRequiresBranchScope() {
        when(accessPolicy.canManageBranch(ACTOR, BRANCH)).thenReturn(false);

        assertError(() -> service.searchTenants(BRANCH, null, null, PageRequest.of(0, 20)), ErrorCode.UNAUTHORIZED);
        verify(tenantRepository, never()).findByBranch(any(), any());
    }

    @Test
    void branchLookupReturnsOneBasedPage() {
        when(accessPolicy.canManageBranch(ACTOR, BRANCH)).thenReturn(true);
        when(tenantRepository.findByBranch(any(), any(Pageable.class)))
                .thenAnswer(call -> new PageImpl<>(List.of(tenant()), call.getArgument(1), 1));

        var page = service.searchTenants(BRANCH, null, null, PageRequest.of(0, 20));

        assertThat(page.getNumber()).isEqualTo(1);
        assertThat(page.getContent()).extracting(TenantResponse::fullName).containsExactly("Tenant A");
    }

    @Test
    void roomLookupUsesRoomScope() {
        when(accessPolicy.canManageRoom(ACTOR, ROOM)).thenReturn(true);
        when(tenantRepository.findByRoom(any(), any(Pageable.class)))
                .thenAnswer(call -> new PageImpl<>(List.of(tenant()), call.getArgument(1), 1));

        assertThat(service.searchTenants(null, ROOM, null, PageRequest.of(0, 20)).getTotalElements()).isEqualTo(1);
    }

    @Test
    void contractLookupChecksTheContractsOwnBranch() {
        when(tenantRepository.findBranchIdByContractId(CONTRACT)).thenReturn(Optional.of(BRANCH));
        when(accessPolicy.canManageBranch(ACTOR, BRANCH)).thenReturn(false);

        assertError(() -> service.searchTenants(null, null, CONTRACT, PageRequest.of(0, 20)), ErrorCode.UNAUTHORIZED);

        when(tenantRepository.findBranchIdByContractId(CONTRACT)).thenReturn(Optional.empty());
        assertError(() -> service.searchTenants(null, null, CONTRACT, PageRequest.of(0, 20)),
                ErrorCode.CONTRACT_NOT_FOUND);
    }

    @Test
    void exactlyOneFilterIsRequired() {
        assertError(() -> service.searchTenants(null, null, null, PageRequest.of(0, 20)), ErrorCode.INVALID_REQUEST);
        assertError(() -> service.searchTenants(BRANCH, ROOM, null, PageRequest.of(0, 20)), ErrorCode.INVALID_REQUEST);
    }

    @Test
    void myTenanciesAreScopedToTheCaller() {
        when(tenantRepository.findByTenantUser(any(), any(Pageable.class)))
                .thenAnswer(call -> new PageImpl<>(List.of(tenant()), call.getArgument(1), 1));

        service.myTenancies(PageRequest.of(0, 20));

        verify(tenantRepository).findByTenantUser(any(), any(Pageable.class));
    }

    @Test
    void occupancyHistoryIsVisibleToTheMemberAndTheRoomManagerOnly() {
        UUID memberId = UUID.randomUUID();
        UUID otherUser = UUID.randomUUID();
        ContractTenant member = ContractTenant.builder().id(memberId).tenantUserId(otherUser).roomId(ROOM).build();
        when(tenantRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(accessPolicy.canManageRoom(ACTOR, ROOM)).thenReturn(false);
        assertError(() -> service.getOccupancies(memberId), ErrorCode.UNAUTHORIZED);

        when(accessPolicy.canManageRoom(ACTOR, ROOM)).thenReturn(true);
        when(occupancyRepository.findByContractTenantIdOrderByStartDateAsc(memberId)).thenReturn(List.of(
                RoomSlotOccupancy.builder().id(UUID.randomUUID()).roomId(ROOM).contractTenantId(memberId)
                        .startDate(LocalDate.of(2026, 1, 1)).endDate(LocalDate.of(2026, 6, 1)).build()));
        List<OccupancyResponse> history = service.getOccupancies(memberId);
        assertThat(history).hasSize(1);
        assertThat(history.get(0).startDate()).isEqualTo(LocalDate.of(2026, 1, 1));
    }

    @Test
    void memberSeesOwnOccupancyWithoutManagerScope() {
        UUID memberId = UUID.randomUUID();
        when(tenantRepository.findById(memberId)).thenReturn(Optional.of(
                ContractTenant.builder().id(memberId).tenantUserId(ACTOR).roomId(ROOM).build()));
        when(occupancyRepository.findByContractTenantIdOrderByStartDateAsc(memberId)).thenReturn(List.of());

        assertThat(service.getOccupancies(memberId)).isEmpty();
    }

    @Test
    void unknownMemberIsNotFound() {
        UUID memberId = UUID.randomUUID();
        when(tenantRepository.findById(memberId)).thenReturn(Optional.empty());

        assertError(() -> service.getOccupancies(memberId), ErrorCode.CONTRACT_TENANT_NOT_FOUND);
    }

    private static TenantResponse tenant() {
        return new TenantResponse(UUID.randomUUID(), CONTRACT, BRANCH, ROOM, null, UUID.randomUUID(), "Tenant A",
                ContractTenantRole.REPRESENTATIVE, ContractTenantStatus.ACTIVE, LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31), null);
    }

    private static void assertError(org.assertj.core.api.ThrowableAssert.ThrowingCallable call, ErrorCode code) {
        assertThatThrownBy(call).isInstanceOf(AppException.class)
                .extracting(e -> ((AppException) e).getErrorCode()).isEqualTo(code);
    }
}
