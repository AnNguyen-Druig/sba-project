package com.sba.project.controller;

import com.sba.project.dto.request.RoomRequest;
import com.sba.project.dto.request.RoomSearchRequest;
import com.sba.project.dto.response.RoomResponse;
import com.sba.project.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @PostMapping
    public ResponseEntity<RoomResponse> create(@Valid @RequestBody RoomRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roomService.create(request));
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<RoomResponse> getById(@PathVariable UUID roomId) {
        return ResponseEntity.ok(roomService.getById(roomId));
    }

    @GetMapping
    public ResponseEntity<Page<RoomResponse>> search(@Valid @ModelAttribute RoomSearchRequest criteria,
                                                     Pageable pageable) {
        return ResponseEntity.ok(roomService.search(criteria, pageable));
    }

    @PutMapping("/{roomId}")
    public ResponseEntity<RoomResponse> update(@PathVariable UUID roomId,
                                               @Valid @RequestBody RoomRequest request) {
        return ResponseEntity.ok(roomService.update(roomId, request));
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> delete(@PathVariable UUID roomId) {
        roomService.delete(roomId);
        return ResponseEntity.noContent().build();
    }
}
