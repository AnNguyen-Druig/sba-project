package com.sba.project.modules.property.service;

import com.sba.project.modules.property.dto.request.RoomRequest;
import com.sba.project.modules.property.dto.request.RoomSearchRequest;
import com.sba.project.modules.property.dto.request.RoomSlotRequest;
import com.sba.project.modules.property.dto.request.RoomTypeRequest;
import com.sba.project.modules.property.dto.response.PublicRoomResponse;
import com.sba.project.modules.property.dto.response.RoomResponse;
import com.sba.project.modules.property.dto.response.RoomSlotResponse;
import com.sba.project.modules.property.dto.response.RoomTypeResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RoomService {
    RoomResponse create(RoomRequest request);
    RoomResponse getById(UUID id);
    Page<RoomResponse> search(RoomSearchRequest criteria, Pageable pageable);
    RoomResponse update(UUID id, RoomRequest request);
    void delete(UUID id);
    Page<PublicRoomResponse> searchPublicRooms(RoomSearchRequest criteria, Pageable pageable);
    PublicRoomResponse getPublicRoomById(UUID id);
    RoomSlotResponse createRoomSlot(RoomSlotRequest request);
    RoomSlotResponse getRoomSlotById(UUID id);
    List<RoomSlotResponse> listRoomSlotsByRoom(UUID roomId);
    RoomSlotResponse updateRoomSlot(UUID id, RoomSlotRequest request);
    void deleteRoomSlot(UUID id);
    RoomTypeResponse createRoomType(RoomTypeRequest request);
    RoomTypeResponse getRoomTypeById(UUID id);
    Page<RoomTypeResponse> listRoomTypes(Pageable pageable);
    RoomTypeResponse updateRoomType(UUID id, RoomTypeRequest request);
    void deleteRoomType(UUID id);
}
