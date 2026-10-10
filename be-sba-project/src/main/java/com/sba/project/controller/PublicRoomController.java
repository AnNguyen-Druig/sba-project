package com.sba.project.controller;

import com.sba.project.dto.request.RoomSearchRequest;
import com.sba.project.dto.response.PublicRoomResponse;
import com.sba.project.service.PublicRoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/public/rooms")
@RequiredArgsConstructor
public class PublicRoomController {

    private final PublicRoomService publicRoomService;

    @GetMapping
    public ResponseEntity<Page<PublicRoomResponse>> search(@Valid @ModelAttribute RoomSearchRequest criteria,
                                                           Pageable pageable) {
        return ResponseEntity.ok(publicRoomService.search(criteria, pageable));
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<PublicRoomResponse> getById(@PathVariable UUID roomId) {
        return ResponseEntity.ok(publicRoomService.getById(roomId));
    }
}
