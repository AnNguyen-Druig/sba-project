package com.sba.project.modules.leasing.service.impl;

import com.sba.project.common.base.PagedResponse;
import com.sba.project.common.exceptions.AppException;
import com.sba.project.common.exceptions.ErrorCode;
import com.sba.project.modules.leasing.dto.response.OccupancyResponse;
import com.sba.project.modules.leasing.dto.response.TenantResponse;
import com.sba.project.modules.leasing.entity.ContractTenant;
import com.sba.project.modules.leasing.mapper.TenantMapper;
import com.sba.project.modules.leasing.repository.OccupancyReadRepository;
import com.sba.project.modules.leasing.repository.TenantDirectoryRepository;
import com.sba.project.modules.leasing.service.TenantAccessPolicy;
import com.sba.project.modules.leasing.service.TenantDirectoryService;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TenantDirectoryServiceImpl implements TenantDirectoryService {
    private final TenantDirectoryRepository tenantRepository;
    private final OccupancyReadRepository occupancyRepository;
    private final TenantMapper mapper;
    private final TenantAccessPolicy accessPolicy;

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<TenantResponse> searchTenants(UUID branchId, UUID roomId, UUID contractId,
            Pageable pageable) {
        UUID actorId = accessPolicy.currentUserId();
        if (Stream.of(branchId, roomId, contractId).filter(id -> id != null).count() != 1) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        Pageable fixedOrder = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        Page<TenantResponse> page;
        if (branchId != null) {
            requireBranchScope(actorId, branchId);
            page = tenantRepository.findByBranch(branchId, fixedOrder);
        } else if (roomId != null) {
            if (!accessPolicy.canManageRoom(actorId, roomId)) throw new AppException(ErrorCode.UNAUTHORIZED);
            page = tenantRepository.findByRoom(roomId, fixedOrder);
        } else {
            UUID contractBranch = tenantRepository.findBranchIdByContractId(contractId)
                    .orElseThrow(() -> new AppException(ErrorCode.CONTRACT_NOT_FOUND));
            requireBranchScope(actorId, contractBranch);
            page = tenantRepository.findByContract(contractId, fixedOrder);
        }
        return toPaged(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<TenantResponse> myTenancies(Pageable pageable) {
        UUID actorId = accessPolicy.currentUserId();
        return toPaged(tenantRepository.findByTenantUser(actorId,
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize())));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OccupancyResponse> getOccupancies(UUID contractTenantId) {
        UUID actorId = accessPolicy.currentUserId();
        ContractTenant member = tenantRepository.findById(contractTenantId)
                .orElseThrow(() -> new AppException(ErrorCode.CONTRACT_TENANT_NOT_FOUND));
        if (!actorId.equals(member.getTenantUserId()) && !accessPolicy.canManageRoom(actorId, member.getRoomId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return occupancyRepository.findByContractTenantIdOrderByStartDateAsc(contractTenantId).stream()
                .map(mapper::toOccupancy).toList();
    }

    private void requireBranchScope(UUID actorId, UUID branchId) {
        if (!accessPolicy.canManageBranch(actorId, branchId)) throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    private static PagedResponse<TenantResponse> toPaged(Page<TenantResponse> page) {
        return PagedResponse.of(page.getContent(), page.getNumber() + 1, page.getSize(), page.getTotalElements());
    }
}
