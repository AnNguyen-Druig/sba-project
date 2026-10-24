package com.sba.project.modules.utilities.controller;

import com.sba.project.common.base.ApiResponse;
import com.sba.project.modules.utilities.dto.request.CreateRoomServiceRequest;
import com.sba.project.modules.utilities.dto.request.UpdateRoomServiceRequest;
import com.sba.project.modules.utilities.dto.response.RoomServiceResponse;
import com.sba.project.modules.utilities.enums.RoomServiceStatus;
import com.sba.project.modules.utilities.service.UtilityServiceManager;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
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
 * REST controller for configuring and updating room-level services [P1-69].
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RoomServiceController {

    private final UtilityServiceManager utilityServiceManager;

    /**
     * [P1-69] Configures pricing and billing configuration for a service in a room.
     */
    @PostMapping("/branches/{branchId}/room-services")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public ResponseEntity<ApiResponse<RoomServiceResponse>> configureRoomService(
            @PathVariable UUID branchId,
            @Valid @RequestBody CreateRoomServiceRequest request) {

        RoomServiceResponse response = utilityServiceManager.configureRoomService(branchId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Room service configured successfully", response));
    }

    /**
     * [P1-69] Updates an existing room service configuration.
     */
    @PutMapping("/room-services/{roomServiceId}")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public ResponseEntity<ApiResponse<RoomServiceResponse>> updateRoomService(
            @PathVariable UUID roomServiceId,
            @Valid @RequestBody UpdateRoomServiceRequest request) {

        RoomServiceResponse response = utilityServiceManager.updateRoomService(roomServiceId, request);
        return ResponseEntity.ok(ApiResponse.success("Room service updated successfully", response));
    }

    /**
     * Lists all service configurations assigned to a room.
     */
    @GetMapping("/rooms/{roomId}/services")
    public ResponseEntity<ApiResponse<List<RoomServiceResponse>>> listRoomServices(@PathVariable UUID roomId) {
        List<RoomServiceResponse> response = utilityServiceManager.listRoomServices(roomId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Lists all service configurations across a branch.
     */
    @GetMapping("/branches/{branchId}/room-services")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<List<RoomServiceResponse>>> listBranchRoomServices(
            @PathVariable UUID branchId,
            @RequestParam(required = false) RoomServiceStatus status) {

        List<RoomServiceResponse> response = utilityServiceManager.listBranchRoomServices(branchId, status);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
