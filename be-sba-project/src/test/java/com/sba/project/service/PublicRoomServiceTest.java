package com.sba.project.service;

import com.sba.project.dto.request.RoomSearchRequest;
import com.sba.project.dto.response.PublicRoomResponse;
import com.sba.project.entity.Branch;
import com.sba.project.entity.Manager;
import com.sba.project.entity.Room;
import com.sba.project.entity.RoomType;
import com.sba.project.exception.BusinessException;
import com.sba.project.exception.ResourceNotFoundException;
import com.sba.project.mapper.RoomMapper;
import com.sba.project.mapper.RoomSlotMapper;
import com.sba.project.mapper.RoomTypeMapper;
import com.sba.project.repository.RoomRepository;
import com.sba.project.repository.RoomSlotRepository;
import com.sba.project.repository.RoomTypeRepository;
import com.sba.project.service.impl.RoomServiceImpl;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PublicRoomServiceTest {

    private RoomRepository roomRepository;
    private RoomServiceImpl service;
    private Branch branch;
    private RoomType roomType;
    private Room room;

    @BeforeEach
    void setUp() {
        roomRepository = mock(RoomRepository.class);
        service = new RoomServiceImpl(roomRepository, mock(RoomTypeRepository.class), mock(RoomSlotRepository.class),
                mock(EntityManager.class), new RoomMapper(), new RoomSlotMapper(), new RoomTypeMapper());
        branch = Branch.builder().branchId(UUID.randomUUID()).branchName("Central").address("Main St").build();
        roomType = RoomType.builder().roomTypeId(UUID.randomUUID()).typeName("Studio").build();
        room = Room.builder().roomId(UUID.randomUUID()).branch(branch)
                .manager(Manager.builder().managerId(UUID.randomUUID()).build()).roomType(roomType)
                .roomCode("A-01").referencePrice(new BigDecimal("100.00")).capacity(2)
                .status("AVAILABLE").description("Bright").build();
    }

    @Test
    void search_unsorted_appliesDefaultRoomCodeSort() {
        when(roomRepository.search(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(room)));

        service.searchPublicRooms(RoomSearchRequest.builder().build(), PageRequest.of(0, 10));

        var captor = org.mockito.ArgumentCaptor.forClass(Pageable.class);
        verify(roomRepository).search(any(), any(), any(), any(), any(), any(), any(), captor.capture());
        assertEquals(Sort.by("roomCode"), captor.getValue().getSort());
    }

    @Test
    void search_allowedSortIsPreserved() {
        when(roomRepository.search(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));
        Pageable pageable = PageRequest.of(0, 10, Sort.by("referencePrice").descending());

        service.searchPublicRooms(RoomSearchRequest.builder().build(), pageable);

        verify(roomRepository).search(any(), any(), any(), any(), any(), any(), any(), org.mockito.ArgumentMatchers.eq(pageable));
    }

    @Test
    void search_disallowedSortProperty_throws400() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("managerId"));

        assertThrows(BusinessException.class, () -> service.searchPublicRooms(RoomSearchRequest.builder().build(), pageable));
    }

    @Test
    void search_minGreaterThanMax_throws400() {
        RoomSearchRequest criteria = RoomSearchRequest.builder()
                .minPrice(new BigDecimal("200")).maxPrice(new BigDecimal("100")).build();

        assertThrows(BusinessException.class, () -> service.searchPublicRooms(criteria, PageRequest.of(0, 10)));
    }

    @Test
    void getById_notFound_throws404() {
        UUID id = UUID.randomUUID();
        when(roomRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getPublicRoomById(id));
    }

    @Test
    void getById_returnsOnlyPublicFields() {
        when(roomRepository.findById(room.getRoomId())).thenReturn(Optional.of(room));

        PublicRoomResponse response = service.getPublicRoomById(room.getRoomId());

        assertEquals(branch.getBranchName(), response.getBranchName());
        assertEquals(branch.getAddress(), response.getAddress());
        assertEquals(roomType.getTypeName(), response.getTypeName());
        assertEquals(room.getRoomCode(), response.getRoomCode());
    }
}
