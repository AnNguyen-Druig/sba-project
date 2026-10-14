package com.sba.project.modules.community.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Create/replace body for P3-01. */
public record ThreadRequest(
        @NotBlank String title,
        @NotBlank String description,
        @NotNull @Valid ThreadCriteria criteria) {
}
