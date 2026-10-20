package com.sba.project.modules.maintenance.service.impl;

import com.sba.project.common.exceptions.AppException;
import com.sba.project.common.exceptions.ErrorCode;
import com.sba.project.modules.maintenance.dto.response.FurnitureFailureResponse;
import com.sba.project.modules.maintenance.dto.response.FurnitureWarrantyResponse;
import com.sba.project.modules.maintenance.entity.FurnitureWarranty;
import com.sba.project.modules.maintenance.mapper.FurnitureMapperImpl;
import com.sba.project.modules.maintenance.repository.FurnitureRepository;
import com.sba.project.modules.maintenance.repository.FurnitureWarrantyRepository;
import com.sba.project.modules.maintenance.repository.ReportRequestFurnitureRepository;
import com.sba.project.modules.maintenance.service.MaintenanceAccessPolicy;
import java.time.LocalDate;
import java.util.List;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** P2-29: expiring warranties and frequently failing furniture. */
@ExtendWith(MockitoExtension.class)
class FurnitureInsightsTests {
    private static final UUID ACTOR = UUID.randomUUID();
    private static final UUID ROOM = UUID.randomUUID();

    @Mock FurnitureRepository furnitureRepository;
    @Mock FurnitureWarrantyRepository warrantyRepository;
    @Mock ReportRequestFurnitureRepository linkRepository;
    @Mock MaintenanceAccessPolicy accessPolicy;
    FurnitureServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new FurnitureServiceImpl(furnitureRepository, warrantyRepository, linkRepository,
                new FurnitureMapperImpl(), accessPolicy);
        when(accessPolicy.currentUserId()).thenReturn(ACTOR);
    }

    @Test
    void expiringWarrantiesCoverTodayThroughTheRequestedWindow() {
        when(accessPolicy.canManageRoom(ACTOR, ROOM)).thenReturn(true);
        when(warrantyRepository.findExpiringInRoom(eq(ROOM), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(FurnitureWarranty.builder().id(UUID.randomUUID()).furnitureId(UUID.randomUUID())
                        .provider("Vendor").startDate(LocalDate.now().minusYears(1))
                        .endDate(LocalDate.now().plusDays(10)).build()));

        List<FurnitureWarrantyResponse> result = service.getExpiringWarranties(ROOM, 30);

        ArgumentCaptor<LocalDate> to = ArgumentCaptor.forClass(LocalDate.class);
        verify(warrantyRepository).findExpiringInRoom(eq(ROOM), eq(LocalDate.now()), to.capture());
        assertThat(to.getValue()).isEqualTo(LocalDate.now().plusDays(30));
        assertThat(result).hasSize(1);
    }

    @Test
    void frequentlyFailingFurnitureIsReturnedFromTheRepositoryCounts() {
        when(accessPolicy.canManageRoom(ACTOR, ROOM)).thenReturn(true);
        FurnitureFailureResponse row = new FurnitureFailureResponse(UUID.randomUUID(), 4);
        when(linkRepository.findFrequentlyFailing(ROOM, 3)).thenReturn(List.of(row));

        assertThat(service.getFrequentlyFailing(ROOM, 3)).containsExactly(row);
    }

    @Test
    void insightsRequireRoomScopeAndSaneThresholds() {
        when(accessPolicy.canManageRoom(ACTOR, ROOM)).thenReturn(false);
        assertError(() -> service.getExpiringWarranties(ROOM, 30), ErrorCode.UNAUTHORIZED);
        assertError(() -> service.getFrequentlyFailing(ROOM, 3), ErrorCode.UNAUTHORIZED);

        when(accessPolicy.canManageRoom(ACTOR, ROOM)).thenReturn(true);
        assertError(() -> service.getExpiringWarranties(ROOM, 0), ErrorCode.INVALID_REQUEST);
        assertError(() -> service.getFrequentlyFailing(ROOM, 1), ErrorCode.INVALID_REQUEST);
        verify(warrantyRepository, never()).findExpiringInRoom(any(), any(), any());
        verify(linkRepository, never()).findFrequentlyFailing(any(), anyLong());
    }

    private static void assertError(org.assertj.core.api.ThrowableAssert.ThrowingCallable call, ErrorCode code) {
        assertThatThrownBy(call).isInstanceOf(AppException.class)
                .extracting(e -> ((AppException) e).getErrorCode()).isEqualTo(code);
    }
}
