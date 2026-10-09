package com.sba.project.service;

import com.sba.project.dto.request.*;
import com.sba.project.dto.response.*;
import com.sba.project.enums.AllocationStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Phân bổ chi phí dịch vụ cho người ở ghép (BE-04.3)
 */
public interface CostAllocationService {

    // Tạo đề xuất phân bổ (Tenant)
    CostAllocationResponse createProposal(CreateCostAllocationRequest request, UUID proposedBy);

    // Duyệt/từ chối đề xuất (Manager/Owner)
    CostAllocationResponse processProposal(UUID proposalId, ProcessAllocationRequest request, UUID processedBy);

    // Xem đề xuất theo phòng
    PageResponse<CostAllocationResponse> getProposalsByRoom(UUID roomId, AllocationStatus status, Pageable pageable);

    // Xem đề xuất theo Tenant
    List<CostAllocationResponse> getProposalsByTenant(UUID tenantId);

    // Xem đề xuất theo charge
    List<CostAllocationResponse> getProposalsByCharge(UUID chargeId);

    // Phân bổ mặc định (chia đều)
    CostAllocationResponse createDefaultAllocation(UUID chargeId, List<UUID> tenantIds, UUID createdBy);
}
