package com.sba.project.modules.community.controller;

import com.sba.project.common.base.ApiResponse;
import com.sba.project.common.base.PagedResponse;
import com.sba.project.modules.community.dto.request.ThreadRequest;
import com.sba.project.modules.community.dto.response.ThreadResponse;
import com.sba.project.modules.community.enums.ThreadStatus;
import com.sba.project.modules.community.service.ThreadService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Phase 3 Thread basics. Login is required for every endpoint (BR-19); page is 1-based. */
@RestController
@RequestMapping("/api/threads")
@RequiredArgsConstructor
public class ThreadController {
    private final ThreadService threadService;

    @PostMapping
    public ResponseEntity<ApiResponse<ThreadResponse>> create(@Valid @RequestBody ThreadRequest request) {
        ApiResponse<ThreadResponse> body = ApiResponse.success(threadService.create(request));
        body.setStatus(HttpStatus.CREATED.value());
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    /** P3-03: browse OPEN threads of everyone. */
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<ThreadResponse>>> browse(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        return ResponseEntity.ok(ApiResponse.success(threadService.browse(keyword,
                PageRequest.of(page - 1, size, Sort.by(direction, sortBy)))));
    }

    /** P3-01: the caller's own threads, any status. */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<PagedResponse<ThreadResponse>>> mine(
            @RequestParam(required = false) ThreadStatus status,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        return ResponseEntity.ok(ApiResponse.success(threadService.listMine(status,
                PageRequest.of(page - 1, size, Sort.by(direction, sortBy)))));
    }

    @GetMapping("/{threadId}")
    public ResponseEntity<ApiResponse<ThreadResponse>> get(@PathVariable UUID threadId) {
        return ResponseEntity.ok(ApiResponse.success(threadService.get(threadId)));
    }

    @PutMapping("/{threadId}")
    public ResponseEntity<ApiResponse<ThreadResponse>> update(@PathVariable UUID threadId,
            @Valid @RequestBody ThreadRequest request) {
        return ResponseEntity.ok(ApiResponse.success(threadService.update(threadId, request)));
    }

    @PostMapping("/{threadId}/close")
    public ResponseEntity<ApiResponse<ThreadResponse>> close(@PathVariable UUID threadId) {
        return ResponseEntity.ok(ApiResponse.success(threadService.close(threadId)));
    }
}
