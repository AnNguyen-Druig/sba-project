package com.sba.project.service;

import com.sba.project.dto.request.RoomRequest;
import com.sba.project.dto.request.RoomSearchRequest;
import com.sba.project.dto.response.RoomResponse;
import com.sba.project.entity.Branch;
import com.sba.project.entity.Manager;
import com.sba.project.entity.Room;
import com.sba.project.entity.RoomType;
import com.sba.project.exception.BusinessException;
import com.sba.project.exception.DuplicateResourceException;
import com.sba.project.exception.ResourceNotFoundException;
import com.sba.project.mapper.RoomMapper;
import com.sba.project.repository.RoomRepository;
import com.sba.project.repository.RoomTypeRepository;
import com.sba.project.service.impl.RoomServiceImpl;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RoomServiceTest {

    private RoomRepository roomRepository;
    private RoomTypeRepository roomTypeRepository;
    private EntityManager entityManager;
    private RoomServiceImpl service;
    private Branch branch;
    private Manager manager;
    private RoomType roomType;

    @BeforeEach
    void setUp() {
        roomRepository = mock(RoomRepository.class);
        roomTypeRepository = mock(RoomTypeRepository.class);
        entityManager = mock(EntityManager.class);
        service = new RoomServiceImpl(roomRepository, roomTypeRepository, entityManager, new RoomMapper());
        branch = Branch.builder().branchId(UUID.randomUUID()).branchName("Central").address("Main St").build();
        manager = Manager.builder().managerId(UUID.randomUUID()).build();
        roomType = RoomType.builder().roomTypeId(UUID.randomUUID()).typeName("Studio").build();
    }

    @Test
    void create_ok() {
        RoomRequest request = request(branch, manager, roomType, "A-01");
        stubReferences(request);
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RoomResponse response = service.create(request);

        assertEquals("A-01", response.getRoomCode());
        verify(roomRepository).existsByBranch_BranchIdAndRoomCode(branch.getBranchId(), "A-01");
    }

    @Test
    void create_duplicateRoomCodeInSameBranch_throws409() {
        RoomRequest request = request(branch, manager, roomType, "A-01");
        stubReferences(request);
        when(roomRepository.existsByBranch_BranchIdAndRoomCode(branch.getBranchId(), "A-01")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> service.create(request));
    }

    @Test
    void create_sameCodeDifferentBranch_ok() {
        Branch otherBranch = Branch.builder().branchId(UUID.randomUUID()).branchName("West").build();
        RoomRequest request = request(otherBranch, manager, roomType, "A-01");
        stubReferences(request);
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertEquals("A-01", service.create(request).getRoomCode());
        verify(roomRepository).existsByBranch_BranchIdAndRoomCode(otherBranch.getBranchId(), "A-01");
    }

    @Test
    void create_unknownBranch_throws404() {
        RoomRequest request = request(branch, manager, roomType, "A-01");
        when(entityManager.find(Branch.class, branch.getBranchId())).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> service.create(request));
    }

    @Test
    void create_unknownManager_throws404() {
        RoomRequest request = request(branch, manager, roomType, "A-01");
        when(entityManager.find(Branch.class, branch.getBranchId())).thenReturn(branch);
        when(entityManager.find(Manager.class, manager.getManagerId())).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> service.create(request));
    }

    @Test
    void create_unknownRoomType_throws404() {
        RoomRequest request = request(branch, manager, roomType, "A-01");
        when(entityManager.find(Branch.class, branch.getBranchId())).thenReturn(branch);
        when(entityManager.find(Manager.class, manager.getManagerId())).thenReturn(manager);
        when(roomTypeRepository.findById(roomType.getRoomTypeId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.create(request));
    }

    @Test
    void create_saveRace_throws409() {
        RoomRequest request = request(branch, manager, roomType, "A-01");
        stubReferences(request);
        when(roomRepository.save(any(Room.class))).thenThrow(new DataIntegrityViolationException("unique"));

        assertThrows(DuplicateResourceException.class, () -> service.create(request));
    }

    @Test
    void update_changeToExistingCode_throws409() {
        UUID id = UUID.randomUUID();
        Room existing = room(id, branch, manager, roomType, "A-01");
        RoomRequest request = request(branch, manager, roomType, "A-02");
        when(roomRepository.findById(id)).thenReturn(Optional.of(existing));
        stubReferences(request);
        when(roomRepository.existsByBranch_BranchIdAndRoomCode(branch.getBranchId(), "A-02")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> service.update(id, request));
    }

    @Test
    void update_sameCode_ok() {
        UUID id = UUID.randomUUID();
        Room existing = room(id, branch, manager, roomType, "A-01");
        RoomRequest request = request(branch, manager, roomType, "A-01");
        when(roomRepository.findById(id)).thenReturn(Optional.of(existing));
        stubReferences(request);
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertEquals("A-01", service.update(id, request).getRoomCode());
        verify(roomRepository, never()).existsByBranch_BranchIdAndRoomCode(any(), any());
    }

    @Test
    void search_blankAddress_treatedAsNull() {
        RoomSearchRequest criteria = RoomSearchRequest.builder().address("  ").status(" ").build();
        PageRequest pageable = PageRequest.of(0, 10);
        when(roomRepository.search(null, null, null, null, null, null, null, pageable))
                .thenReturn(new PageImpl<>(List.of()));

        service.search(criteria, pageable);

        verify(roomRepository).search(null, null, null, null, null, null, null, pageable);
    }

    @Test
    void search_minGreaterThanMax_throws400() {
        RoomSearchRequest criteria = RoomSearchRequest.builder()
                .minPrice(new BigDecimal("200")).maxPrice(new BigDecimal("100")).build();

        assertThrows(BusinessException.class, () -> service.search(criteria, PageRequest.of(0, 10)));
    }

    @Test
    void search_passesFiltersInCorrectOrder() {
        BigDecimal min = new BigDecimal("100");
        BigDecimal max = new BigDecimal("500");
        RoomSearchRequest criteria = RoomSearchRequest.builder().branchId(branch.getBranchId())
                .roomTypeId(roomType.getRoomTypeId()).minPrice(min).maxPrice(max).minCapacity(2)
                .status(" ACTIVE ").address(" Main ").build();
        PageRequest pageable = PageRequest.of(0, 10);
        when(roomRepository.search(branch.getBranchId(), roomType.getRoomTypeId(), min, max, 2,
                "ACTIVE", "Main", pageable)).thenReturn(new PageImpl<>(List.of()));

        service.search(criteria, pageable);

        verify(roomRepository).search(branch.getBranchId(), roomType.getRoomTypeId(), min, max, 2,
                "ACTIVE", "Main", pageable);
    }

    @Test
    void delete_fkViolation_throws409() {
        UUID id = UUID.randomUUID();
        when(roomRepository.findById(id)).thenReturn(Optional.of(room(id, branch, manager, roomType, "A-01")));
        doThrow(new DataIntegrityViolationException("referenced")).when(roomRepository).flush();

        assertThrows(DuplicateResourceException.class, () -> service.delete(id));
    }

    private void stubReferences(RoomRequest request) {
        when(entityManager.find(Branch.class, request.getBranchId())).thenReturn(branch.getBranchId().equals(request.getBranchId()) ? branch : Branch.builder().branchId(request.getBranchId()).build());
        when(entityManager.find(Manager.class, request.getManagerId())).thenReturn(manager);
        when(roomTypeRepository.findById(request.getRoomTypeId())).thenReturn(Optional.of(roomType));
    }

    private RoomRequest request(Branch requestBranch, Manager requestManager, RoomType requestType, String code) {
        return RoomRequest.builder().branchId(requestBranch.getBranchId()).managerId(requestManager.getManagerId())
                .roomTypeId(requestType.getRoomTypeId()).roomCode(code).referencePrice(new BigDecimal("100.00"))
                .capacity(2).status("AVAILABLE").description("Bright").build();
    }

    private Room room(UUID id, Branch roomBranch, Manager roomManager, RoomType type, String code) {
        return Room.builder().roomId(id).branch(roomBranch).manager(roomManager).roomType(type)
                .roomCode(code).referencePrice(new BigDecimal("100.00")).capacity(2)
                .status("AVAILABLE").description("Bright").build();
    }
}
