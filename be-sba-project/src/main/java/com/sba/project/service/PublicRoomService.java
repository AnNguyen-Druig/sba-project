package com.sba.project.service;

import com.sba.project.dto.request.RoomSearchRequest;
import com.sba.project.dto.response.PublicRoomResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PublicRoomService {

    Page<PublicRoomResponse> search(RoomSearchRequest criteria, Pageable pageable);

    PublicRoomResponse getById(UUID roomId);
}
