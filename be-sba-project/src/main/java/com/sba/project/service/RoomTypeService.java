package com.sba.project.service;

import com.sba.project.dto.request.RoomTypeRequest;
import com.sba.project.dto.response.RoomTypeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface RoomTypeService {

    RoomTypeResponse create(RoomTypeRequest request);

    RoomTypeResponse getById(UUID roomTypeId);

    Page<RoomTypeResponse> list(Pageable pageable);

    RoomTypeResponse update(UUID roomTypeId, RoomTypeRequest request);

    void delete(UUID roomTypeId);
}
