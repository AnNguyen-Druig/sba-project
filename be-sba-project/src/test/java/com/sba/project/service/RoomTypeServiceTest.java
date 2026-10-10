package com.sba.project.service;

import com.sba.project.dto.request.RoomTypeRequest;
import com.sba.project.dto.response.RoomTypeResponse;
import com.sba.project.entity.RoomType;
import com.sba.project.exception.DuplicateResourceException;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RoomTypeServiceTest {

    private RoomTypeRepository repository;
    private RoomRepository roomRepository;
    private RoomServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(RoomTypeRepository.class);
        roomRepository = mock(RoomRepository.class);
        service = new RoomServiceImpl(roomRepository, repository, mock(RoomSlotRepository.class),
                mock(EntityManager.class), new RoomMapper(), new RoomSlotMapper(), new RoomTypeMapper());
    }

    @Test
    void create_savesAndReturnsResponse() {
        UUID id = UUID.randomUUID();
        when(repository.save(any(RoomType.class))).thenAnswer(invocation -> {
            RoomType entity = invocation.getArgument(0);
            entity.setRoomTypeId(id);
            return entity;
        });

        RoomTypeResponse response = service.createRoomType(request("Studio", 2));

        assertEquals(id, response.getRoomTypeId());
        assertEquals("Studio", response.getTypeName());
        verify(repository).save(any(RoomType.class));
    }

    @Test
    void getById_returnsResponse() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(roomType(id, "Studio")));

        assertEquals("Studio", service.getRoomTypeById(id).getTypeName());
    }

    @Test
    void getById_notFound_throws404() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getRoomTypeById(id));
    }

    @Test
    void list_mapsPageContent() {
        when(repository.findAll(PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(roomType(UUID.randomUUID(), "Studio"))));

        assertEquals("Studio", service.listRoomTypes(PageRequest.of(0, 10)).getContent().getFirst().getTypeName());
    }

    @Test
    void update_changesFields() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(roomType(id, "Studio")));
        when(repository.save(any(RoomType.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RoomTypeResponse response = service.updateRoomType(id, request("Suite", 3));

        assertEquals("Suite", response.getTypeName());
        assertEquals(3, response.getDefaultCapacity());
    }

    @Test
    void delete_whenReferenced_throws409() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(roomType(id, "Studio")));
        when(roomRepository.existsReferencedByAnyRoom(id)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> service.deleteRoomType(id));
        verify(repository, never()).delete(any(RoomType.class));
    }

    @Test
    void delete_unreferenced_marksTypeDeletedWithoutRemovingRow() {
        UUID id = UUID.randomUUID();
        RoomType roomType = roomType(id, "Studio");
        when(repository.findById(id)).thenReturn(Optional.of(roomType));
        when(roomRepository.existsReferencedByAnyRoom(id)).thenReturn(false);

        service.deleteRoomType(id);

        assertTrue(roomType.isDeleted());
        verify(repository, never()).delete(any(RoomType.class));
        verify(repository, never()).flush();
    }

    @Test
    void delete_notFound_throws404() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.deleteRoomType(id));
    }

    private RoomTypeRequest request(String name, int capacity) {
        return RoomTypeRequest.builder().typeName(name).defaultCapacity(capacity).build();
    }

    private RoomType roomType(UUID id, String name) {
        return RoomType.builder().roomTypeId(id).typeName(name).defaultCapacity(2).build();
    }
}
