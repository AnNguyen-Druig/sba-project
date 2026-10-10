package com.sba.project.controller;
import com.sba.project.dto.request.*;
import com.sba.project.dto.response.*;
import com.sba.project.service.MeterAndChargeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MeterAndChargeController {

    private final MeterAndChargeService meterAndChargeService;

    @PostMapping("/meter-readings")

    public ResponseEntity<RoomServiceChargeResponse> createMeterReading(
            @Valid @RequestBody CreateMeterReadingRequest request,
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(meterAndChargeService.createMeterReading(request, userId));
    }

    @PostMapping("/service-charges")

    public ResponseEntity<RoomServiceChargeResponse> createServiceCharge(
            @Valid @RequestBody CreateServiceChargeRequest request,
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(meterAndChargeService.createServiceCharge(request, userId));
    }

    @PatchMapping("/service-charges/{chargeId}/finalize")

    public ResponseEntity<RoomServiceChargeResponse> finalizeCharge(
            @PathVariable UUID chargeId,
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(meterAndChargeService.finalizeCharge(chargeId, userId));
    }

    @GetMapping("/rooms/{roomId}/service-charges")

    public ResponseEntity<List<RoomServiceChargeResponse>> getChargesByRoomAndPeriod(
            @PathVariable UUID roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(meterAndChargeService.getChargesByRoomAndPeriod(roomId, from, to));
    }

    @GetMapping("/rooms/{roomId}/service-charges/unfinalized")

    public ResponseEntity<List<RoomServiceChargeResponse>> getUnfinalizedCharges(@PathVariable UUID roomId) {
        return ResponseEntity.ok(meterAndChargeService.getUnfinalizedCharges(roomId));
    }

    @GetMapping("/rooms/{roomId}/service-charges/all")

    public ResponseEntity<PageResponse<RoomServiceChargeResponse>> getChargesByRoomPaged(
            @PathVariable UUID roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("billingPeriodStart").descending());
        return ResponseEntity.ok(meterAndChargeService.getChargesByRoom(roomId, pageable));
    }
}
