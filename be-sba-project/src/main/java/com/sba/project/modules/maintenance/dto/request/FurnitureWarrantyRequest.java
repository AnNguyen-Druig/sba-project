package com.sba.project.modules.maintenance.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/** P1-108: provider, period, terms and warranty code. */
public record FurnitureWarrantyRequest(
        @NotBlank String provider,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        String terms,
        String warrantyCode) {
}
