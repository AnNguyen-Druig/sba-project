package com.sba.project.controller;

import com.sba.project.dto.request.RoomSlotRequest;
import com.sba.project.dto.response.RoomSlotResponse;
import com.sba.project.service.RoomSlotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class RoomSlotController {

    private final RoomSlotService roomSlotService;

    @PostMapping("/room-slots")
    public ResponseEntity<RoomSlotResponse> create(@Valid @RequestBody RoomSlotRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roomSlotService.create(request));
    }

    @GetMapping("/room-slots/{roomSlotId}")
    public ResponseEntity<RoomSlotResponse> getById(@PathVariable UUID roomSlotId) {
        return ResponseEntity.ok(roomSlotService.getById(roomSlotId));
    }

    @GetMapping("/rooms/{roomId}/slots")
    public ResponseEntity<List<RoomSlotResponse>> listByRoom(@PathVariable UUID roomId) {
        return ResponseEntity.ok(roomSlotService.listByRoom(roomId));
    }

    @PutMapping("/room-slots/{roomSlotId}")
    public ResponseEntity<RoomSlotResponse> update(@PathVariable UUID roomSlotId,
                                                   @Valid @RequestBody RoomSlotRequest request) {
        return ResponseEntity.ok(roomSlotService.update(roomSlotId, request));
    }

    @DeleteMapping("/room-slots/{roomSlotId}")
    public ResponseEntity<Void> delete(@PathVariable UUID roomSlotId) {
        roomSlotService.delete(roomSlotId);
        return ResponseEntity.noContent().build();
    }
}
