package com.sba.project.modules.property.controller;

import com.sba.project.common.base.ApiResponse;
import com.sba.project.modules.property.dto.request.RoomSlotRequest;
import com.sba.project.modules.property.dto.response.RoomSlotResponse;
import com.sba.project.modules.property.service.RoomService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1") @RequiredArgsConstructor
public class RoomSlotController {
    private final RoomService service;
    @PostMapping("/room-slots") public ResponseEntity<ApiResponse<RoomSlotResponse>> create(@Valid @RequestBody RoomSlotRequest r) { return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.createRoomSlot(r))); }
    @GetMapping("/room-slots/{id}") public ResponseEntity<ApiResponse<RoomSlotResponse>> get(@PathVariable UUID id) { return ResponseEntity.ok(ApiResponse.success(service.getRoomSlotById(id))); }
    @GetMapping("/rooms/{roomId}/slots") public ResponseEntity<ApiResponse<List<RoomSlotResponse>>> list(@PathVariable UUID roomId) { return ResponseEntity.ok(ApiResponse.success(service.listRoomSlotsByRoom(roomId))); }
    @PutMapping("/room-slots/{id}") public ResponseEntity<ApiResponse<RoomSlotResponse>> update(@PathVariable UUID id, @Valid @RequestBody RoomSlotRequest r) { return ResponseEntity.ok(ApiResponse.success(service.updateRoomSlot(id, r))); }
    @DeleteMapping("/room-slots/{id}") public ResponseEntity<Void> delete(@PathVariable UUID id) { service.deleteRoomSlot(id); return ResponseEntity.noContent().build(); }
}
