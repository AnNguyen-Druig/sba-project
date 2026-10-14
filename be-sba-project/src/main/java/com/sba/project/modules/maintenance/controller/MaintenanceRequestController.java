package com.sba.project.modules.maintenance.controller;

import com.sba.project.common.base.ApiResponse;
import com.sba.project.common.base.PagedResponse;
import com.sba.project.modules.maintenance.dto.request.AssignRequest;
import com.sba.project.modules.maintenance.dto.request.CreateMaintenanceRequest;
import com.sba.project.modules.maintenance.dto.request.EvidenceRequest;
import com.sba.project.modules.maintenance.dto.request.LiabilityRequest;
import com.sba.project.modules.maintenance.dto.request.MaintenanceRecordRequest;
import com.sba.project.modules.maintenance.dto.request.TransitionRequest;
import com.sba.project.modules.maintenance.dto.response.MaintenanceRecordResponse;
import com.sba.project.modules.maintenance.dto.response.MaintenanceRequestDetailResponse;
import com.sba.project.modules.maintenance.dto.response.MaintenanceRequestSummaryResponse;
import com.sba.project.modules.maintenance.enums.ReportRequestPriority;
import com.sba.project.modules.maintenance.enums.ReportRequestStatus;
import com.sba.project.modules.maintenance.service.MaintenanceRecordService;
import com.sba.project.modules.maintenance.service.MaintenanceRequestService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import com.sba.project.modules.maintenance.dto.request.RatingRequest;
import com.sba.project.modules.maintenance.dto.response.RepairCostStatResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** BE-06.4 repair requests. Authorization is enforced in the services (tenant ownership, Branch scope). */
@RestController
@RequestMapping("/api/maintenance-requests")
@RequiredArgsConstructor
public class MaintenanceRequestController {
    private final MaintenanceRequestService requestService;
    private final MaintenanceRecordService recordService;

    @PostMapping
    public ResponseEntity<ApiResponse<MaintenanceRequestDetailResponse>> create(
            @Valid @RequestBody CreateMaintenanceRequest request) {
        return created(requestService.createRequest(request));
    }

    /** P1-119: the caller's own requests. Page is 1-based. */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<PagedResponse<MaintenanceRequestSummaryResponse>>> myRequests(
            @RequestParam(required = false) ReportRequestStatus status,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        return ResponseEntity.ok(ApiResponse.success(requestService.listMyRequests(status,
                PageRequest.of(page - 1, size, Sort.by(direction, sortBy)))));
    }

    /** P1-114: Owner/Manager view of one room's requests. */
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<MaintenanceRequestSummaryResponse>>> roomRequests(
            @RequestParam UUID roomId,
            @RequestParam(required = false) ReportRequestStatus status,
            @RequestParam(required = false) ReportRequestPriority priority,
            @RequestParam(defaultValue = "false") boolean overdue,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        return ResponseEntity.ok(ApiResponse.success(requestService.searchRoomRequests(roomId, status, priority,
                overdue, PageRequest.of(page - 1, size, Sort.by(direction, sortBy)))));
    }

    /** P2-32: approved repair liabilities per room and party; give exactly one of roomId or branchId. */
    @GetMapping("/cost-stats")
    public ResponseEntity<ApiResponse<List<RepairCostStatResponse>>> costStats(
            @RequestParam(required = false) UUID roomId, @RequestParam(required = false) UUID branchId) {
        return ok(requestService.costStats(roomId, branchId));
    }

    /** P2-31: the requester rates a completed request once. */
    @PostMapping("/{requestId}/rating")
    public ResponseEntity<ApiResponse<MaintenanceRequestDetailResponse>> rate(@PathVariable UUID requestId,
            @Valid @RequestBody RatingRequest request) {
        return ok(requestService.rate(requestId, request));
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<ApiResponse<MaintenanceRequestDetailResponse>> get(@PathVariable UUID requestId) {
        return ok(requestService.getRequest(requestId));
    }

    @PostMapping("/{requestId}/accept")
    public ResponseEntity<ApiResponse<MaintenanceRequestDetailResponse>> accept(@PathVariable UUID requestId,
            @RequestBody(required = false) TransitionRequest request) {
        return ok(requestService.accept(requestId, request));
    }

    @PostMapping("/{requestId}/reject")
    public ResponseEntity<ApiResponse<MaintenanceRequestDetailResponse>> reject(@PathVariable UUID requestId,
            @RequestBody(required = false) TransitionRequest request) {
        return ok(requestService.reject(requestId, request));
    }

    @PostMapping("/{requestId}/start")
    public ResponseEntity<ApiResponse<MaintenanceRequestDetailResponse>> start(@PathVariable UUID requestId,
            @RequestBody(required = false) TransitionRequest request) {
        return ok(requestService.start(requestId, request));
    }

    @PostMapping("/{requestId}/complete")
    public ResponseEntity<ApiResponse<MaintenanceRequestDetailResponse>> complete(@PathVariable UUID requestId,
            @RequestBody(required = false) TransitionRequest request) {
        return ok(requestService.complete(requestId, request));
    }

    @PostMapping("/{requestId}/assign")
    public ResponseEntity<ApiResponse<MaintenanceRequestDetailResponse>> assign(@PathVariable UUID requestId,
            @Valid @RequestBody AssignRequest request) {
        return ok(requestService.assign(requestId, request));
    }

    @PostMapping("/{requestId}/evidence")
    public ResponseEntity<ApiResponse<MaintenanceRequestDetailResponse>> addEvidence(@PathVariable UUID requestId,
            @Valid @RequestBody EvidenceRequest request) {
        return ok(requestService.addEvidence(requestId, request));
    }

    @PostMapping("/{requestId}/records")
    public ResponseEntity<ApiResponse<MaintenanceRecordResponse>> addRecord(@PathVariable UUID requestId,
            @Valid @RequestBody MaintenanceRecordRequest request) {
        return created(recordService.addRecord(requestId, request));
    }

    @PostMapping("/{requestId}/records/{costRecordId}/liabilities")
    public ResponseEntity<ApiResponse<MaintenanceRecordResponse>> addLiability(@PathVariable UUID requestId,
            @PathVariable UUID costRecordId, @Valid @RequestBody LiabilityRequest request) {
        return created(recordService.addLiability(requestId, costRecordId, request));
    }

    @PostMapping("/{requestId}/records/{recordId}/approve")
    public ResponseEntity<ApiResponse<MaintenanceRecordResponse>> approveRecord(@PathVariable UUID requestId,
            @PathVariable UUID recordId) {
        return ok(recordService.approveRecord(requestId, recordId));
    }

    @PostMapping("/{requestId}/records/{recordId}/reject")
    public ResponseEntity<ApiResponse<MaintenanceRecordResponse>> rejectRecord(@PathVariable UUID requestId,
            @PathVariable UUID recordId) {
        return ok(recordService.rejectRecord(requestId, recordId));
    }

    private static <T> ResponseEntity<ApiResponse<T>> ok(T data) {
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    private static <T> ResponseEntity<ApiResponse<T>> created(T data) {
        ApiResponse<T> body = ApiResponse.success(data);
        body.setStatus(HttpStatus.CREATED.value());
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }
}
