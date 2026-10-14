package com.sba.project.modules.leasing.dto.response;

import com.sba.project.modules.leasing.enums.ContractTenantRole;
import com.sba.project.modules.leasing.enums.ContractTenantStatus;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Rental relationship (CONTRACT_TENANTS) joined with the shared USERS profile. Only the display name
 * is exposed: Owner/Manager see rental data in scope, never the global profile (P1-31).
 */
public record TenantResponse(
        UUID contractTenantId,
        UUID contractId,
        UUID branchId,
        UUID roomId,
        UUID roomSlotId,
        UUID tenantUserId,
        String fullName,
        ContractTenantRole role,
        ContractTenantStatus status,
        LocalDate startDate,
        LocalDate endDate,
        LocalDate actualLeaveDate) {
}
