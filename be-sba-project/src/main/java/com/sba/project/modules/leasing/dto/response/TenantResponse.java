package com.sba.project.modules.leasing.dto.response;

import com.sba.project.modules.leasing.enums.ContractStatus;
import com.sba.project.modules.leasing.enums.ContractTenantRole;
import com.sba.project.modules.leasing.enums.ContractTenantStatus;
import com.sba.project.modules.leasing.enums.RentalMode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Rental relationship (CONTRACT_TENANTS) joined with the shared USERS profile. Only the display name
 * is exposed: Owner/Manager see rental data in scope, never the global profile (P1-31). Branch, room,
 * slot and contract labels plus the member's own rent/terms are rental data, not profile data.
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
        LocalDate actualLeaveDate,
        String branchName,
        String roomCode,
        String slotName,
        String contractCode,
        ContractStatus contractStatus,
        RentalMode rentalMode,
        BigDecimal monthlyRent,
        BigDecimal firstMonthRent,
        BigDecimal lastMonthRent,
        BigDecimal prepaidFirstRequired,
        BigDecimal prepaidLastRequired,
        String terms,
        String contractTerms) {
}
