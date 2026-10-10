package com.sba.project.service;

import com.sba.project.dto.request.RoomSlotRequest;
import com.sba.project.dto.response.RoomSlotResponse;

import java.util.List;
import java.util.UUID;

public interface RoomSlotService {

    RoomSlotResponse create(RoomSlotRequest request);

    RoomSlotResponse getById(UUID roomSlotId);

    List<RoomSlotResponse> listByRoom(UUID roomId);

    RoomSlotResponse update(UUID roomSlotId, RoomSlotRequest request);

    void delete(UUID roomSlotId);
}
