package com.sba.project.modules.leasing.service;

import com.sba.project.common.base.PagedResponse;
import com.sba.project.modules.leasing.dto.response.OccupancyResponse;
import com.sba.project.modules.leasing.dto.response.TenantResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

/** BE-06.2 read side: tenant lookup (P1-31/32), own tenancies (P1-36) and occupancy history (P1-34). */
public interface TenantDirectoryService {
    /** Exactly one of branchId / roomId / contractId must be provided. */
    PagedResponse<TenantResponse> searchTenants(UUID branchId, UUID roomId, UUID contractId, Pageable pageable);

    PagedResponse<TenantResponse> myTenancies(Pageable pageable);

    List<OccupancyResponse> getOccupancies(UUID contractTenantId);
}
