package com.sba.project.controller;
import com.sba.project.dto.request.*;
import com.sba.project.dto.response.*;
import com.sba.project.enums.ServiceStatus;
import com.sba.project.enums.ServiceType;
import com.sba.project.service.ServiceManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceManagementService serviceManagementService;

    @PostMapping("/services")

    public ResponseEntity<ServiceResponse> createService(
            @Valid @RequestBody CreateServiceRequest request,
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(serviceManagementService.createService(request, userId));
    }

    @PutMapping("/services/{serviceId}")

    public ResponseEntity<ServiceResponse> updateService(
            @PathVariable UUID serviceId,
            @Valid @RequestBody UpdateServiceRequest request,
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(serviceManagementService.updateService(serviceId, request, userId));
    }

    @GetMapping("/services/{serviceId}")

    public ResponseEntity<ServiceResponse> getService(@PathVariable UUID serviceId) {
        return ResponseEntity.ok(serviceManagementService.getServiceById(serviceId));
    }

    @GetMapping("/branches/{branchId}/services")

    public ResponseEntity<PageResponse<ServiceResponse>> getServicesByBranch(
            @PathVariable UUID branchId,
            @RequestParam(required = false) ServiceStatus status,
            @RequestParam(required = false) ServiceType serviceType,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(serviceManagementService.getServicesByBranch(branchId, status, serviceType, keyword, pageable));
    }

    @GetMapping("/branches/{branchId}/services/defaults")

    public ResponseEntity<List<ServiceResponse>> getDefaultServices(@PathVariable UUID branchId) {
        return ResponseEntity.ok(serviceManagementService.getDefaultServicesByBranch(branchId));
    }

    @PostMapping("/room-services")

    public ResponseEntity<RoomServiceResponse> assignServiceToRoom(
            @Valid @RequestBody AssignRoomServiceRequest request,
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(serviceManagementService.assignServiceToRoom(request, userId));
    }

    @PatchMapping("/room-services/{roomServiceId}/price")

    public ResponseEntity<RoomServiceResponse> updateRoomServicePrice(
            @PathVariable UUID roomServiceId,
            @RequestParam BigDecimal unitPrice,
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(serviceManagementService.updateRoomServicePrice(roomServiceId, unitPrice, userId));
    }

    @GetMapping("/rooms/{roomId}/services")

    public ResponseEntity<List<RoomServiceResponse>> getRoomServices(@PathVariable UUID roomId) {
        return ResponseEntity.ok(serviceManagementService.getRoomServices(roomId));
    }

    @DeleteMapping("/room-services/{roomServiceId}")

    public ResponseEntity<Void> deactivateRoomService(
            @PathVariable UUID roomServiceId,
            @RequestHeader("X-User-Id") UUID userId) {
        serviceManagementService.deactivateRoomService(roomServiceId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/contract-services")

    public ResponseEntity<ContractServiceResponse> registerContractService(
            @Valid @RequestBody RegisterContractServiceRequest request,
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(serviceManagementService.registerContractService(request, userId));
    }

    @PatchMapping("/contract-services/{contractServiceId}/cancel")

    public ResponseEntity<ContractServiceResponse> cancelContractService(
            @PathVariable UUID contractServiceId,
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(serviceManagementService.cancelContractServiceRegistration(contractServiceId, userId));
    }

    @PatchMapping("/contract-services/{contractServiceId}/process")

    public ResponseEntity<ContractServiceResponse> processContractService(
            @PathVariable UUID contractServiceId,
            @RequestParam boolean approved,
            @RequestParam(required = false) String rejectionReason,
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(
            serviceManagementService.processContractServiceRegistration(contractServiceId, approved, rejectionReason, userId));
    }

    @GetMapping("/contracts/{contractId}/services")

    public ResponseEntity<List<ContractServiceResponse>> getContractServices(@PathVariable UUID contractId) {
        return ResponseEntity.ok(serviceManagementService.getContractServices(contractId));
    }
}
