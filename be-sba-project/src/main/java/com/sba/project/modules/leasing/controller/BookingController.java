package com.sba.project.modules.leasing.controller;

import com.sba.project.common.base.ApiResponse;
import com.sba.project.common.base.PagedResponse;
import com.sba.project.modules.leasing.dto.request.BookingCreateRequest;
import com.sba.project.modules.leasing.dto.request.BookingDecisionRequest;
import com.sba.project.modules.leasing.dto.request.BookingSearchRequest;
import com.sba.project.modules.leasing.dto.response.BookingResponse;
import com.sba.project.modules.leasing.enums.BookingStatus;
import com.sba.project.modules.leasing.service.BookingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** BE-03.1 Booking. Caller identity and Branch scope come from the token, never from the body. */
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {
    private final BookingService bookingService;

    /** P1-49/P1-50: hold a Room/Slot; response carries the hold amount and payment deadline. */
    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> create(@Valid @RequestBody BookingCreateRequest request) {
        ApiResponse<BookingResponse> body = ApiResponse.success(bookingService.create(request));
        body.setStatus(HttpStatus.CREATED.value());
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    /** P1-51: the caller's own Bookings; page is 1-based like PagedResponse. */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<PagedResponse<BookingResponse>>> myBookings(
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(ApiResponse.success(
                bookingService.myBookings(status, PageRequest.of(page - 1, size))));
    }

    /** P1-51: Owner/Manager list of one Branch; {@code from}/{@code to} are inclusive stay days. */
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<BookingResponse>>> search(
            @RequestParam UUID branchId,
            @RequestParam(required = false) UUID roomId,
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(ApiResponse.success(bookingService.search(
                new BookingSearchRequest(branchId, roomId, status, from, to), PageRequest.of(page - 1, size))));
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<ApiResponse<BookingResponse>> getById(@PathVariable UUID bookingId) {
        return ResponseEntity.ok(ApiResponse.success(bookingService.getById(bookingId)));
    }

    /** P1-51: there is no "approve" command — availability is checked automatically (BR-P09). */
    @PostMapping("/{bookingId}/reject")
    public ResponseEntity<ApiResponse<BookingResponse>> reject(@PathVariable UUID bookingId,
            @Valid @RequestBody BookingDecisionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(bookingService.reject(bookingId, request)));
    }
}
