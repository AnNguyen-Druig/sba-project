package com.sba.project.modules.property.controller;

import com.sba.project.common.base.ApiResponse;
import com.sba.project.common.base.PagedResponse;
import com.sba.project.modules.property.dto.request.RoomTypeRequest;
import com.sba.project.modules.property.dto.response.RoomTypeResponse;
import com.sba.project.modules.property.service.RoomService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/room-types") @RequiredArgsConstructor
public class RoomTypeController {
    private final RoomService service;
    @PostMapping public ResponseEntity<ApiResponse<RoomTypeResponse>> create(@Valid @RequestBody RoomTypeRequest r) { return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.createRoomType(r))); }
    @GetMapping("/{id}") public ResponseEntity<ApiResponse<RoomTypeResponse>> get(@PathVariable UUID id) { return ResponseEntity.ok(ApiResponse.success(service.getRoomTypeById(id))); }
    @GetMapping public ResponseEntity<ApiResponse<PagedResponse<RoomTypeResponse>>> list(Pageable pageable) {
        Page<RoomTypeResponse> page = service.listRoomTypes(pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.of(page.getContent(), page.getNumber() + 1, page.getSize(), page.getTotalElements())));
    }
    @PutMapping("/{id}") public ResponseEntity<ApiResponse<RoomTypeResponse>> update(@PathVariable UUID id, @Valid @RequestBody RoomTypeRequest r) { return ResponseEntity.ok(ApiResponse.success(service.updateRoomType(id, r))); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable UUID id) { service.deleteRoomType(id); return ResponseEntity.noContent().build(); }
}
