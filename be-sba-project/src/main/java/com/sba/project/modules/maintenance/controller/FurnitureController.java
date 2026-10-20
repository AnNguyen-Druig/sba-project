package com.sba.project.modules.maintenance.controller;

import com.sba.project.common.base.ApiResponse;
import com.sba.project.common.base.PagedResponse;
import com.sba.project.modules.maintenance.dto.request.FurnitureCreateRequest;
import com.sba.project.modules.maintenance.dto.request.FurnitureUpdateRequest;
import com.sba.project.modules.maintenance.dto.request.FurnitureWarrantyRequest;
import com.sba.project.modules.maintenance.dto.response.FurnitureFailureResponse;
import com.sba.project.modules.maintenance.dto.response.FurnitureResponse;
import com.sba.project.modules.maintenance.dto.response.FurnitureWarrantyResponse;
import com.sba.project.modules.maintenance.enums.FurnitureStatus;
import com.sba.project.modules.maintenance.service.FurnitureService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** BE-06.3 Furniture/CSVC and warranties. No hard delete: retire through status (P1-110). */
@RestController
@RequestMapping("/api/furnitures")
@RequiredArgsConstructor
public class FurnitureController {
    private final FurnitureService furnitureService;

    @PostMapping
    public ResponseEntity<ApiResponse<FurnitureResponse>> createFurniture(
            @Valid @RequestBody FurnitureCreateRequest request) {
        return created(furnitureService.createFurniture(request));
    }

    /** P1-109; page is 1-based like PagedResponse. */
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<FurnitureResponse>>> searchFurniture(
            @RequestParam UUID roomId,
            @RequestParam(required = false) FurnitureStatus status,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "assetCode") String sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction direction) {
        PageRequest pageable = PageRequest.of(page - 1, size, Sort.by(direction, sortBy));
        return ResponseEntity.ok(ApiResponse.success(
                furnitureService.searchFurniture(roomId, status, type, pageable)));
    }

    /** P2-29: warranties of the room's furniture that end within {@code withinDays} days. */
    @GetMapping("/warranties/expiring")
    public ResponseEntity<ApiResponse<List<FurnitureWarrantyResponse>>> expiringWarranties(
            @RequestParam UUID roomId, @RequestParam @Min(1) @Max(366) int withinDays) {
        return ResponseEntity.ok(ApiResponse.success(furnitureService.getExpiringWarranties(roomId, withinDays)));
    }

    /** P2-29: furniture reported in at least {@code minRequests} repair requests. */
    @GetMapping("/frequent-failures")
    public ResponseEntity<ApiResponse<List<FurnitureFailureResponse>>> frequentFailures(
            @RequestParam UUID roomId, @RequestParam @Min(2) int minRequests) {
        return ResponseEntity.ok(ApiResponse.success(furnitureService.getFrequentlyFailing(roomId, minRequests)));
    }

    @GetMapping("/{furnitureId}")
    public ResponseEntity<ApiResponse<FurnitureResponse>> getFurniture(@PathVariable UUID furnitureId) {
        return ResponseEntity.ok(ApiResponse.success(furnitureService.getFurniture(furnitureId)));
    }

    @PutMapping("/{furnitureId}")
    public ResponseEntity<ApiResponse<FurnitureResponse>> updateFurniture(@PathVariable UUID furnitureId,
            @Valid @RequestBody FurnitureUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(furnitureService.updateFurniture(furnitureId, request)));
    }

    @GetMapping("/{furnitureId}/warranties")
    public ResponseEntity<ApiResponse<List<FurnitureWarrantyResponse>>> getWarranties(
            @PathVariable UUID furnitureId) {
        return ResponseEntity.ok(ApiResponse.success(furnitureService.getWarranties(furnitureId)));
    }

    @PostMapping("/{furnitureId}/warranties")
    public ResponseEntity<ApiResponse<FurnitureWarrantyResponse>> addWarranty(@PathVariable UUID furnitureId,
            @Valid @RequestBody FurnitureWarrantyRequest request) {
        return created(furnitureService.addWarranty(furnitureId, request));
    }

    @PutMapping("/{furnitureId}/warranties/{warrantyId}")
    public ResponseEntity<ApiResponse<FurnitureWarrantyResponse>> updateWarranty(@PathVariable UUID furnitureId,
            @PathVariable UUID warrantyId, @Valid @RequestBody FurnitureWarrantyRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                furnitureService.updateWarranty(furnitureId, warrantyId, request)));
    }

    @DeleteMapping("/{furnitureId}/warranties/{warrantyId}")
    public ResponseEntity<ApiResponse<Void>> deleteWarranty(@PathVariable UUID furnitureId,
            @PathVariable UUID warrantyId) {
        furnitureService.deleteWarranty(furnitureId, warrantyId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /** ApiResponse helpers default to 200; the body status must match the 201 response. */
    private static <T> ResponseEntity<ApiResponse<T>> created(T data) {
        ApiResponse<T> body = ApiResponse.success(data);
        body.setStatus(HttpStatus.CREATED.value());
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }
}
