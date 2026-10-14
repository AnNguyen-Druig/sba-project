package com.sba.project.modules.maintenance.dto.request;

import com.sba.project.modules.maintenance.enums.ReportRecordRecordType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

/**
 * P1-116. Only WORK and COST records are written here; ASSIGNMENT/STATUS records are produced by
 * assign/transition actions and LIABILITY records by the liability endpoint. COST requires amount.
 */
public record MaintenanceRecordRequest(
        @NotNull ReportRecordRecordType recordType,
        @NotBlank String description,
        @PositiveOrZero @Digits(integer = 19, fraction = 0) BigDecimal amount,
        @Size(max = 20) String statusAfter,
        @Valid List<EvidenceRequest> evidence) {
}
