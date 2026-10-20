package com.sba.project.modules.identity.controller;

import com.sba.project.common.base.ApiResponse;
import com.sba.project.common.base.PagedResponse;
import com.sba.project.modules.identity.dto.request.OwnerVerificationDecisionRequest;
import com.sba.project.modules.identity.dto.response.OwnerVerificationResponse;
import com.sba.project.modules.identity.enums.OwnerVerificationStatus;
import com.sba.project.modules.identity.service.OwnerVerificationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** ADMIN-only Owner verification review (P1-08, P1-132); the ADMIN check lives in the service. */
@RestController
@RequestMapping("/api/admin/owner-verifications")
@RequiredArgsConstructor
public class OwnerVerificationAdminController {
    private final OwnerVerificationService service;

    /** Defaults to PENDING. Page is 1-based. */
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<OwnerVerificationResponse>>> list(
            @RequestParam(required = false) OwnerVerificationStatus status,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction direction) {
        return ResponseEntity.ok(ApiResponse.success(service.list(status,
                PageRequest.of(page - 1, size, Sort.by(direction, sortBy)))));
    }

    @PostMapping("/{userId}/approve")
    public ResponseEntity<ApiResponse<OwnerVerificationResponse>> approve(@PathVariable UUID userId,
            @Valid @RequestBody OwnerVerificationDecisionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.approve(userId, request)));
    }

    @PostMapping("/{userId}/reject")
    public ResponseEntity<ApiResponse<OwnerVerificationResponse>> reject(@PathVariable UUID userId,
            @Valid @RequestBody OwnerVerificationDecisionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.reject(userId, request)));
    }
}
