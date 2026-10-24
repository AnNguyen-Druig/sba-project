package com.sba.project.modules.property.controller;

import com.sba.project.common.base.ApiResponse;
import com.sba.project.modules.identity.entity.User;
import com.sba.project.modules.identity.repository.UserRepository;
import com.sba.project.modules.property.dto.response.BranchAssignmentResponse;
import com.sba.project.modules.property.entity.Branch;
import com.sba.project.modules.property.enums.BranchStatus;
import com.sba.project.modules.property.repository.BranchRepository;
import com.sba.project.modules.utilities.util.SecurityUtils;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BranchController {
    private final BranchRepository branchRepository;
    private final UserRepository userRepository;

    @GetMapping("/api/manager/branches")
    public ResponseEntity<ApiResponse<List<BranchAssignmentResponse>>> getManagerBranches() {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        List<Branch> branches;
        if (currentUserId != null) {
            branches = branchRepository.findAll().stream()
                    .filter(b -> currentUserId.equals(b.getManagerId()) || currentUserId.equals(b.getOwnerId()))
                    .toList();
            if (branches.isEmpty()) {
                branches = branchRepository.findAll();
            }
        } else {
            branches = branchRepository.findAll();
        }

        Map<UUID, String> userNames = userRepository.findAll().stream()
                .collect(Collectors.toMap(User::getId, User::getFullName, (k1, k2) -> k1));

        List<BranchAssignmentResponse> response = branches.stream()
                .map(b -> BranchAssignmentResponse.builder()
                        .id(b.getId())
                        .ownerId(b.getOwnerId())
                        .managerId(b.getManagerId())
                        .name(b.getName())
                        .address(b.getAddress())
                        .status(b.getStatus() != null ? b.getStatus() : BranchStatus.ACTIVE)
                        .managerName(b.getManagerId() != null && userNames.containsKey(b.getManagerId())
                                ? userNames.get(b.getManagerId()) : "Quản lý")
                        .zaloUrl(b.getZaloGroupUrl())
                        .zaloEnabled(Boolean.TRUE.equals(b.getZaloEnabled()))
                        .build())
                .toList();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/api/branches")
    public ResponseEntity<ApiResponse<List<BranchAssignmentResponse>>> getAllBranches() {
        return getManagerBranches();
    }
}
