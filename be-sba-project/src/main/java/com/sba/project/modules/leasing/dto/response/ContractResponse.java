package com.sba.project.modules.leasing.dto.response;

import com.sba.project.modules.leasing.enums.ContractDocumentStatus;
import com.sba.project.modules.leasing.enums.ContractStatus;
import com.sba.project.modules.leasing.enums.PaymentEnvironment;
import com.sba.project.modules.leasing.enums.RentalMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Main Contract of a Room (C13) with its participants. {@code lastDay} is inclusive. */
public record ContractResponse(
        UUID id,
        String code,
        UUID ownerId,
        UUID branchId,
        UUID roomId,
        RentalMode rentalMode,
        PaymentEnvironment paymentEnvironment,
        LocalDate startDate,
        LocalDate lastDay,
        String terms,
        ContractStatus status,
        ContractDocumentStatus documentStatus,
        UUID createdBy,
        Long version,
        Instant createdAt,
        Instant updatedAt,
        List<ContractTenantResponse> members) {
}
