package com.sba.project.modules.maintenance.service.impl;

import com.sba.project.common.base.PagedResponse;
import com.sba.project.common.exceptions.AppException;
import com.sba.project.common.exceptions.ErrorCode;
import com.sba.project.modules.maintenance.dto.request.FurnitureCreateRequest;
import com.sba.project.modules.maintenance.dto.request.FurnitureUpdateRequest;
import com.sba.project.modules.maintenance.dto.request.FurnitureWarrantyRequest;
import com.sba.project.modules.maintenance.dto.response.FurnitureResponse;
import com.sba.project.modules.maintenance.dto.response.FurnitureWarrantyResponse;
import com.sba.project.modules.maintenance.entity.Furniture;
import com.sba.project.modules.maintenance.entity.FurnitureWarranty;
import com.sba.project.modules.maintenance.enums.FurnitureStatus;
import com.sba.project.modules.maintenance.mapper.FurnitureMapperImpl;
import com.sba.project.modules.maintenance.repository.FurnitureRepository;
import com.sba.project.modules.maintenance.repository.FurnitureWarrantyRepository;
import com.sba.project.modules.maintenance.repository.ReportRequestFurnitureRepository;
import com.sba.project.modules.maintenance.service.MaintenanceAccessPolicy;
import java.math.BigDecimal;
import java.time.LocalDate;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** The access policy is a Mockito test double, not the production authorization adapter. */
@ExtendWith(MockitoExtension.class)
class FurnitureServiceImplTests {
    private static final UUID ACTOR = UUID.randomUUID();
    private static final UUID ROOM = UUID.randomUUID();

    @Mock FurnitureRepository furnitureRepository;
    @Mock FurnitureWarrantyRepository warrantyRepository;
    @Mock ReportRequestFurnitureRepository reportRequestFurnitureRepository;
    @Mock MaintenanceAccessPolicy accessPolicy;
    FurnitureServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new FurnitureServiceImpl(furnitureRepository, warrantyRepository,
                reportRequestFurnitureRepository, new FurnitureMapperImpl(), accessPolicy);
    }

    @Test
    void createSavesFurnitureForRoomManager() {
        allowManager();
        when(furnitureRepository.save(any(Furniture.class))).thenAnswer(call -> call.getArgument(0));

        FurnitureResponse response = service.createFurniture(createRequest("DESK-1"));

        ArgumentCaptor<Furniture> saved = ArgumentCaptor.forClass(Furniture.class);
        verify(furnitureRepository).save(saved.capture());
        assertThat(saved.getValue().getRoomId()).isEqualTo(ROOM);
        assertThat(saved.getValue().getAssetValue()).isEqualByComparingTo("1500000");
        assertThat(response.assetCode()).isEqualTo("DESK-1");
        assertThat(response.status()).isEqualTo(FurnitureStatus.IN_USE);
    }

    @Test
    void createIsRejectedOutsideBranchScope() {
        when(accessPolicy.currentUserId()).thenReturn(ACTOR);
        when(accessPolicy.canManageRoom(ACTOR, ROOM)).thenReturn(false);

        assertError(() -> service.createFurniture(createRequest("DESK-1")), ErrorCode.UNAUTHORIZED);
        verify(furnitureRepository, never()).save(any());
    }

    @Test
    void unauthenticatedCallerIsRejectedBeforeAnyRepositoryAccess() {
        when(accessPolicy.currentUserId()).thenThrow(new AppException(ErrorCode.UNAUTHENTICATED));

        assertError(() -> service.createFurniture(createRequest("DESK-1")), ErrorCode.UNAUTHENTICATED);
        verify(furnitureRepository, never()).existsByRoomIdAndAssetCode(any(), any());
    }

    @Test
    void createRejectsDuplicateAssetCodeInRoom() {
        allowManager();
        when(furnitureRepository.existsByRoomIdAndAssetCode(ROOM, "DESK-1")).thenReturn(true);

        assertError(() -> service.createFurniture(createRequest("DESK-1")), ErrorCode.FURNITURE_ASSET_CODE_EXISTS);
        verify(furnitureRepository, never()).save(any());
    }

    @Test
    void missingFurnitureIsNotFound() {
        UUID id = UUID.randomUUID();
        when(furnitureRepository.findById(id)).thenReturn(Optional.empty());

        assertError(() -> service.getFurniture(id), ErrorCode.FURNITURE_NOT_FOUND);
    }

    @Test
    void retiringFurnitureLinkedToOpenRepairIsBlocked() {
        Furniture furniture = existingFurniture(FurnitureStatus.DAMAGED);
        allowManager();
        when(reportRequestFurnitureRepository.existsByFurnitureIdAndRequestStatusIn(
                furniture.getId(), FurnitureServiceImpl.OPEN_REPAIR_STATUSES)).thenReturn(true);

        assertError(() -> service.updateFurniture(furniture.getId(), updateRequest("DESK-1", FurnitureStatus.RETIRED)),
                ErrorCode.FURNITURE_IN_OPEN_REPAIR);
        verify(furnitureRepository, never()).save(any());
        assertThat(furniture.getStatus()).isEqualTo(FurnitureStatus.DAMAGED);
    }

    @Test
    void retiringFurnitureWithoutOpenRepairKeepsRoom() {
        Furniture furniture = existingFurniture(FurnitureStatus.IN_USE);
        allowManager();
        when(furnitureRepository.save(furniture)).thenReturn(furniture);

        FurnitureResponse response = service.updateFurniture(furniture.getId(),
                updateRequest("DESK-1", FurnitureStatus.RETIRED));

        assertThat(response.status()).isEqualTo(FurnitureStatus.RETIRED);
        assertThat(response.roomId()).isEqualTo(ROOM);
    }

    @Test
    void updateRejectsAssetCodeUsedByAnotherFurniture() {
        Furniture furniture = existingFurniture(FurnitureStatus.IN_USE);
        allowManager();
        when(furnitureRepository.existsByRoomIdAndAssetCodeAndIdNot(ROOM, "DESK-2", furniture.getId()))
                .thenReturn(true);

        assertError(() -> service.updateFurniture(furniture.getId(), updateRequest("DESK-2", FurnitureStatus.IN_USE)),
                ErrorCode.FURNITURE_ASSET_CODE_EXISTS);
    }

    @Test
    void searchUsesDefaultSortAndReturnsOneBasedPage() {
        allowManager();
        Furniture furniture = Furniture.builder().id(UUID.randomUUID()).roomId(ROOM).assetCode("DESK-1")
                .name("Desk").status(FurnitureStatus.IN_USE).build();
        when(furnitureRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenAnswer(call -> new PageImpl<>(List.of(furniture), call.getArgument(1), 1));

        PagedResponse<FurnitureResponse> page = service.searchFurniture(ROOM, null, "desk", PageRequest.of(0, 20));

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(furnitureRepository).findAll(any(Specification.class), pageable.capture());
        assertThat(pageable.getValue().getSort()).isEqualTo(Sort.by("assetCode"));
        assertThat(page.getNumber()).isEqualTo(1);
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent()).extracting(FurnitureResponse::id).containsExactly(furniture.getId());
    }

    @Test
    void searchRejectsUnsupportedSortProperty() {
        allowManager();

        assertError(() -> service.searchFurniture(ROOM, null, null, PageRequest.of(0, 20, Sort.by("roomId"))),
                ErrorCode.INVALID_REQUEST);
    }

    @Test
    void searchOutsideBranchScopeIsRejected() {
        when(accessPolicy.currentUserId()).thenReturn(ACTOR);

        assertError(() -> service.searchFurniture(ROOM, null, null, PageRequest.of(0, 20)), ErrorCode.UNAUTHORIZED);
        verify(furnitureRepository, never()).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void addWarrantyRejectsEndBeforeStart() {
        Furniture furniture = existingFurniture(FurnitureStatus.IN_USE);
        allowManager();
        var request = new FurnitureWarrantyRequest("Vendor", LocalDate.of(2026, 5, 1), LocalDate.of(2026, 4, 30),
                null, null);

        assertError(() -> service.addWarranty(furniture.getId(), request), ErrorCode.INVALID_WARRANTY_PERIOD);
        verify(warrantyRepository, never()).save(any());
    }

    @Test
    void addWarrantyAttachesToFurniture() {
        Furniture furniture = existingFurniture(FurnitureStatus.IN_USE);
        allowManager();
        when(warrantyRepository.save(any(FurnitureWarranty.class))).thenAnswer(call -> call.getArgument(0));
        LocalDate sameDay = LocalDate.of(2026, 5, 1);

        FurnitureWarrantyResponse response = service.addWarranty(furniture.getId(),
                new FurnitureWarrantyRequest("Vendor", sameDay, sameDay, "Parts only", "W-1"));

        assertThat(response.furnitureId()).isEqualTo(furniture.getId());
        assertThat(response.warrantyCode()).isEqualTo("W-1");
    }

    @Test
    void warrantyOfAnotherFurnitureIsNotFound() {
        Furniture furniture = existingFurniture(FurnitureStatus.IN_USE);
        allowManager();
        UUID warrantyId = UUID.randomUUID();
        when(warrantyRepository.findByIdAndFurnitureId(warrantyId, furniture.getId())).thenReturn(Optional.empty());

        assertError(() -> service.deleteWarranty(furniture.getId(), warrantyId), ErrorCode.FURNITURE_WARRANTY_NOT_FOUND);
        verify(warrantyRepository, never()).delete(any());
    }

    private void allowManager() {
        when(accessPolicy.currentUserId()).thenReturn(ACTOR);
        when(accessPolicy.canManageRoom(eq(ACTOR), eq(ROOM))).thenReturn(true);
    }

    private Furniture existingFurniture(FurnitureStatus status) {
        Furniture furniture = Furniture.builder().id(UUID.randomUUID()).roomId(ROOM).assetCode("DESK-1")
                .name("Desk").status(status).build();
        when(furnitureRepository.findById(furniture.getId())).thenReturn(Optional.of(furniture));
        return furniture;
    }

    private static FurnitureCreateRequest createRequest(String assetCode) {
        return new FurnitureCreateRequest(ROOM, assetCode, "Desk", "Desk", "SN-1", LocalDate.of(2026, 1, 15),
                new BigDecimal("1500000"), "New", FurnitureStatus.IN_USE);
    }

    private static FurnitureUpdateRequest updateRequest(String assetCode, FurnitureStatus status) {
        return new FurnitureUpdateRequest(assetCode, "Desk", "Desk", null, null, null, null, status);
    }

    private static void assertError(org.assertj.core.api.ThrowableAssert.ThrowingCallable call, ErrorCode code) {
        assertThatThrownBy(call).isInstanceOf(AppException.class)
                .extracting(e -> ((AppException) e).getErrorCode()).isEqualTo(code);
    }
}
