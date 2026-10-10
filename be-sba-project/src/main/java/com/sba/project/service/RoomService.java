package com.sba.project.service;

import com.sba.project.dto.request.RoomRequest;
import com.sba.project.dto.request.RoomSearchRequest;
import com.sba.project.dto.response.PublicRoomResponse;
import com.sba.project.dto.response.RoomResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface RoomService {

    RoomResponse create(RoomRequest request);

    RoomResponse getById(UUID roomId);

    Page<RoomResponse> search(RoomSearchRequest criteria, Pageable pageable);

    RoomResponse update(UUID roomId, RoomRequest request);

    void delete(UUID roomId);

    Page<PublicRoomResponse> searchPublicRooms(RoomSearchRequest criteria, Pageable pageable);

    PublicRoomResponse getPublicRoomById(UUID roomId);
}
