package com.sba.project.modules.utilities.dto.response;

import com.sba.project.modules.utilities.enums.ContractServiceStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO representing a contract service registration [P1-71][P1-72].
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractServiceResponse {

    private UUID id;
    private UUID contractId;
    private UUID contractTenantId;
    private UUID roomId;
    private UUID roomServiceId;
    private String serviceName;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal agreedUnitPrice;
    private ContractServiceStatus status;
    private UUID requestedBy;
    private UUID processedBy;
    private Instant processedAt;
    private String reason;
}
