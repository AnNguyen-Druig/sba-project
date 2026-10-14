package com.sba.project.modules.maintenance.dto.request;

import com.sba.project.modules.maintenance.enums.ReportRecordResponsibleParty;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

/** P1-117: split of one COST record between OWNER and TENANT. */
public record LiabilityRequest(
        @NotNull ReportRecordResponsibleParty responsibleParty,
        @NotNull @PositiveOrZero @Digits(integer = 19, fraction = 0) BigDecimal amount,
        @NotBlank String description) {
}
