package com.sba.project.controller;

import com.sba.project.dto.request.RoomTypeRequest;
import com.sba.project.dto.response.RoomTypeResponse;
import com.sba.project.service.RoomTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/room-types")
@RequiredArgsConstructor
public class RoomTypeController {

    private final RoomTypeService roomTypeService;

    @PostMapping
    public ResponseEntity<RoomTypeResponse> create(@Valid @RequestBody RoomTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roomTypeService.create(request));
    }

    @GetMapping("/{roomTypeId}")
    public ResponseEntity<RoomTypeResponse> getById(@PathVariable UUID roomTypeId) {
        return ResponseEntity.ok(roomTypeService.getById(roomTypeId));
    }

    @GetMapping
    public ResponseEntity<Page<RoomTypeResponse>> list(Pageable pageable) {
        return ResponseEntity.ok(roomTypeService.list(pageable));
    }

    @PutMapping("/{roomTypeId}")
    public ResponseEntity<RoomTypeResponse> update(@PathVariable UUID roomTypeId,
                                                   @Valid @RequestBody RoomTypeRequest request) {
        return ResponseEntity.ok(roomTypeService.update(roomTypeId, request));
    }

    @DeleteMapping("/{roomTypeId}")
    public ResponseEntity<Void> delete(@PathVariable UUID roomTypeId) {
        roomTypeService.delete(roomTypeId);
        return ResponseEntity.noContent().build();
    }
}
