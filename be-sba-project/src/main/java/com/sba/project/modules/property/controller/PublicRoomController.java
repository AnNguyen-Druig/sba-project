package com.sba.project.modules.property.controller;

import com.sba.project.common.base.ApiResponse;
import com.sba.project.common.base.PagedResponse;
import com.sba.project.modules.property.dto.request.RoomSearchRequest;
import com.sba.project.modules.property.dto.response.PublicRoomResponse;
import com.sba.project.modules.property.service.RoomService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/public/rooms") @RequiredArgsConstructor
public class PublicRoomController {
    private final RoomService service;
    @GetMapping public ResponseEntity<ApiResponse<PagedResponse<PublicRoomResponse>>> search(@ModelAttribute RoomSearchRequest criteria, Pageable pageable) {
        Page<PublicRoomResponse> page = service.searchPublicRooms(criteria, pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.of(page.getContent(), page.getNumber() + 1, page.getSize(), page.getTotalElements())));
    }
    @GetMapping("/{id}") public ResponseEntity<ApiResponse<PublicRoomResponse>> get(@PathVariable UUID id) { return ResponseEntity.ok(ApiResponse.success(service.getPublicRoomById(id))); }
}
