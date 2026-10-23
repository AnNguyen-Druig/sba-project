package com.sba.project.modules.leasing.service.impl;

import com.sba.project.modules.leasing.repository.ContractTenantRepository;
import com.sba.project.modules.leasing.service.TenancyQueryService;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TenancyQueryServiceImpl implements TenancyQueryService {
    private final ContractTenantRepository contractTenantRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<UUID> findActiveContractTenantId(UUID userId, UUID roomId) {
        return contractTenantRepository.findActiveTenancyIds(userId, roomId).stream().findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasActiveTenancyInBranch(UUID userId, UUID branchId) {
        return contractTenantRepository.existsActiveTenancyInBranch(userId, branchId);
    }
}
