package com.sba.project.modules.property.controller;

import com.sba.project.common.base.ApiResponse;
import com.sba.project.common.base.PagedResponse;
import com.sba.project.modules.property.dto.request.RoomRequest;
import com.sba.project.modules.property.dto.request.RoomSearchRequest;
import com.sba.project.modules.property.dto.response.RoomResponse;
import com.sba.project.modules.property.service.RoomService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/rooms") @RequiredArgsConstructor
public class RoomController {
    private final RoomService service;
    @PostMapping public ResponseEntity<ApiResponse<RoomResponse>> create(@Valid @RequestBody RoomRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.create(request))); }
    @GetMapping("/{id}") public ResponseEntity<ApiResponse<RoomResponse>> get(@PathVariable UUID id) { return ResponseEntity.ok(ApiResponse.success(service.getById(id))); }
    @GetMapping public ResponseEntity<ApiResponse<PagedResponse<RoomResponse>>> search(@ModelAttribute RoomSearchRequest criteria, Pageable pageable) {
        Page<RoomResponse> page = service.search(criteria, pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.of(page.getContent(), page.getNumber() + 1, page.getSize(), page.getTotalElements())));
    }
    @PutMapping("/{id}") public ResponseEntity<ApiResponse<RoomResponse>> update(@PathVariable UUID id, @Valid @RequestBody RoomRequest request) { return ResponseEntity.ok(ApiResponse.success(service.update(id, request))); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable UUID id) { service.delete(id); return ResponseEntity.noContent().build(); }
}
