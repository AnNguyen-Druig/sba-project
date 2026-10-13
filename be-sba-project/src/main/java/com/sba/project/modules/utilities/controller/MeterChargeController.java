package com.sba.project.modules.utilities.controller;

import com.sba.project.common.base.ApiResponse;
import com.sba.project.modules.utilities.dto.request.CreateRoomServiceChargeRequest;
import com.sba.project.modules.utilities.dto.request.FinalizeChargeRequest;
import com.sba.project.modules.utilities.dto.response.ChargeDetailResponse;
import com.sba.project.modules.utilities.dto.response.RoomServiceChargeResponse;
import com.sba.project.modules.utilities.enums.RoomServiceChargeStatus;
import com.sba.project.modules.utilities.service.MeterChargeService;
import com.sba.project.modules.utilities.util.SecurityUtils;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for meter readings and service charges [P1-74][P1-75][P1-78].
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MeterChargeController {

    private final MeterChargeService meterChargeService;

    /**
     * [P1-74] Records a meter reading or periodic room service charge segment.
     */
    @PostMapping("/service-charges")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<RoomServiceChargeResponse>> createCharge(
            @Valid @RequestBody CreateRoomServiceChargeRequest request) {

        UUID currentUserId = SecurityUtils.getCurrentUserId();
        RoomServiceChargeResponse response = meterChargeService.createCharge(currentUserId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Service charge recorded successfully", response));
    }

    /**
     * [P1-75] Finalizes a batch of service charges for invoicing.
     */
    @PutMapping("/service-charges/finalize")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<RoomServiceChargeResponse>>> finalizeCharges(
            @Valid @RequestBody FinalizeChargeRequest request) {

        UUID currentUserId = SecurityUtils.getCurrentUserId();
        List<RoomServiceChargeResponse> response = meterChargeService.finalizeCharges(currentUserId, request);
        return ResponseEntity.ok(ApiResponse.success("Charges finalized successfully", response));
    }

    /**
     * [P1-78] Retrieves charge details by ID.
     */
    @GetMapping("/service-charges/{chargeId}")
    public ResponseEntity<ApiResponse<RoomServiceChargeResponse>> getCharge(@PathVariable UUID chargeId) {
        RoomServiceChargeResponse response = meterChargeService.getCharge(chargeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * [P1-78] Retrieves full calculation breakdown for reconciliation.
     */
    @GetMapping("/service-charges/{chargeId}/detail")
    public ResponseEntity<ApiResponse<ChargeDetailResponse>> getChargeDetail(@PathVariable UUID chargeId) {
        ChargeDetailResponse response = meterChargeService.getChargeDetail(chargeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Lists charges for a specific room and billing month.
     */
    @GetMapping("/rooms/{roomId}/service-charges")
    public ResponseEntity<ApiResponse<List<RoomServiceChargeResponse>>> listCharges(
            @PathVariable UUID roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate billingMonth,
            @RequestParam(required = false) RoomServiceChargeStatus status) {

        List<RoomServiceChargeResponse> response = meterChargeService.listCharges(roomId, billingMonth, status);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
