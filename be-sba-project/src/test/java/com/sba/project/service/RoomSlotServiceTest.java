package com.sba.project.service;

import com.sba.project.dto.request.RoomSlotRequest;
import com.sba.project.dto.response.RoomSlotResponse;
import com.sba.project.entity.Room;
import com.sba.project.entity.RoomSlot;
import com.sba.project.exception.BusinessException;
import com.sba.project.exception.DuplicateResourceException;
import com.sba.project.exception.ResourceNotFoundException;
import com.sba.project.mapper.RoomSlotMapper;
import com.sba.project.repository.RoomRepository;
import com.sba.project.repository.RoomSlotRepository;
import com.sba.project.mapper.RoomMapper;
import com.sba.project.repository.RoomTypeRepository;
import com.sba.project.service.impl.RoomServiceImpl;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RoomSlotServiceTest {

    private RoomSlotRepository slotRepository;
    private RoomRepository roomRepository;
    private RoomServiceImpl service;
    private Room room;

    @BeforeEach
    void setUp() {
        slotRepository = mock(RoomSlotRepository.class);
        roomRepository = mock(RoomRepository.class);
        service = new RoomServiceImpl(roomRepository, mock(RoomTypeRepository.class), slotRepository,
                mock(EntityManager.class), new RoomMapper(), new RoomSlotMapper());
        room = Room.builder().roomId(UUID.randomUUID()).build();
    }

    @Test
    void create_ok() {
        when(roomRepository.findById(room.getRoomId())).thenReturn(Optional.of(room));
        when(slotRepository.save(any(RoomSlot.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RoomSlotResponse response = service.createRoomSlot(request(room.getRoomId(), "B1"));

        assertEquals(room.getRoomId(), response.getRoomId());
        assertEquals("B1", response.getSlotCode());
    }

    @Test
    void create_unknownRoom_throws404() {
        UUID roomId = UUID.randomUUID();
        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.createRoomSlot(request(roomId, "B1")));
    }

    @Test
    void getById_notFound_throws404() {
        UUID id = UUID.randomUUID();
        when(slotRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getRoomSlotById(id));
    }

    @Test
    void listByRoom_unknownRoom_throws404() {
        UUID roomId = UUID.randomUUID();
        when(roomRepository.existsById(roomId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.listRoomSlotsByRoom(roomId));
    }

    @Test
    void listByRoom_returnsRoomSlots() {
        when(roomRepository.findById(room.getRoomId())).thenReturn(Optional.of(room));
        when(slotRepository.findByRoom_RoomId(room.getRoomId())).thenReturn(List.of(slot(room.getRoomId(), "B1")));

        assertEquals("B1", service.listRoomSlotsByRoom(room.getRoomId()).getFirst().getSlotCode());
    }

    @Test
    void update_differentRoom_throws400() {
        UUID id = UUID.randomUUID();
        RoomSlot slot = slot(room.getRoomId(), "B1");
        when(slotRepository.findById(id)).thenReturn(Optional.of(slot));

        assertThrows(BusinessException.class, () -> service.updateRoomSlot(id, request(UUID.randomUUID(), "B2")));
    }

    @Test
    void update_ok() {
        UUID id = UUID.randomUUID();
        when(slotRepository.findById(id)).thenReturn(Optional.of(slot(room.getRoomId(), "B1")));
        when(slotRepository.save(any(RoomSlot.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertEquals("B2", service.updateRoomSlot(id, request(room.getRoomId(), "B2")).getSlotCode());
    }

    @Test
    void delete_fkViolation_throws409() {
        UUID id = UUID.randomUUID();
        when(slotRepository.findById(id)).thenReturn(Optional.of(slot(room.getRoomId(), "B1")));
        doThrow(new DataIntegrityViolationException("referenced")).when(slotRepository).flush();

        assertThrows(DuplicateResourceException.class, () -> service.deleteRoomSlot(id));
    }

    @Test
    void delete_notFound_throws404() {
        UUID id = UUID.randomUUID();
        when(slotRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.deleteRoomSlot(id));
    }

    private RoomSlotRequest request(UUID roomId, String code) {
        return RoomSlotRequest.builder().roomId(roomId).slotCode(code).slotName("Bed")
                .status("AVAILABLE").description("Near window").build();
    }

    private RoomSlot slot(UUID roomId, String code) {
        return RoomSlot.builder().roomSlotId(UUID.randomUUID()).room(Room.builder().roomId(roomId).build())
                .slotCode(code).slotName("Bed").status("AVAILABLE").build();
    }
}
