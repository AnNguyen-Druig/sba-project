package com.sba.project.service;
import com.sba.project.dto.request.*;
import com.sba.project.dto.response.*;
import com.sba.project.enums.AllocationStatus;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.UUID;
public interface CostAllocationService {
    CostAllocationResponse createProposal(CreateCostAllocationRequest request, UUID proposedBy);
    CostAllocationResponse processProposal(UUID proposalId, ProcessAllocationRequest request, UUID processedBy);
    PageResponse<CostAllocationResponse> getProposalsByRoom(UUID roomId, AllocationStatus status, Pageable pageable);
    List<CostAllocationResponse> getProposalsByTenant(UUID tenantId);
    List<CostAllocationResponse> getProposalsByCharge(UUID chargeId);
    CostAllocationResponse createDefaultAllocation(UUID chargeId, List<UUID> tenantIds, UUID createdBy);
}
