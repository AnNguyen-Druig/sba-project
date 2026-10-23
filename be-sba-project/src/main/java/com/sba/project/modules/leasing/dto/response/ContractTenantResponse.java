package com.sba.project.modules.leasing.dto.response;

import com.sba.project.modules.leasing.enums.ContractTenantRole;
import com.sba.project.modules.leasing.enums.ContractTenantStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Contract participant and its own agreed terms (P1-61, P1-63). {@code lastDay} is inclusive. */
public record ContractTenantResponse(
        UUID id,
        UUID contractId,
        UUID tenantUserId,
        UUID roomSlotId,
        UUID sourceBookingId,
        ContractTenantRole role,
        ContractTenantStatus status,
        LocalDate startDate,
        LocalDate lastDay,
        LocalDate actualLeaveDate,
        BigDecimal monthlyRent,
        BigDecimal firstMonthRent,
        BigDecimal lastMonthRent,
        BigDecimal prepaidFirstRequired,
        BigDecimal prepaidLastRequired,
        String terms,
        Long version) {
}
