package com.sba.project.modules.maintenance.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** P2-31. The schema limits the rating to 1..5. */
public record RatingRequest(@NotNull @Min(1) @Max(5) Integer rating, String comment) {
}
