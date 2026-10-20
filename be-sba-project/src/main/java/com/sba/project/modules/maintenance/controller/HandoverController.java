package com.sba.project.modules.maintenance.controller;

import com.sba.project.common.base.ApiResponse;
import com.sba.project.modules.maintenance.dto.request.HandoverRequest;
import com.sba.project.modules.maintenance.dto.response.HandoverResponse;
import com.sba.project.modules.maintenance.service.HandoverService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** P2-19 furniture handover for a rental member. Authorization is enforced in the service. */
@RestController
@RequestMapping("/api/contract-tenants/{contractTenantId}/handover")
@RequiredArgsConstructor
public class HandoverController {
    private final HandoverService handoverService;

    @PostMapping
    public ResponseEntity<ApiResponse<HandoverResponse>> record(@PathVariable UUID contractTenantId,
            @Valid @RequestBody HandoverRequest request) {
        ApiResponse<HandoverResponse> body = ApiResponse.success(handoverService.recordHandover(contractTenantId, request));
        body.setStatus(HttpStatus.CREATED.value());
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<HandoverResponse>> get(@PathVariable UUID contractTenantId) {
        return ResponseEntity.ok(ApiResponse.success(handoverService.getHandover(contractTenantId)));
    }
}
