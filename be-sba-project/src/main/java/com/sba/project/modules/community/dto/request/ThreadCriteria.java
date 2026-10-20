package com.sba.project.modules.community.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** P3-02 criteria: area, price, floor area, capacity, amenities, expected move-in date. All optional. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ThreadCriteria(
        String area,
        @PositiveOrZero BigDecimal minPrice,
        @PositiveOrZero BigDecimal maxPrice,
        @Positive BigDecimal minAreaSqm,
        @Positive Integer capacity,
        List<String> amenities,
        LocalDate expectedMoveInDate) {

    /** Cross-field rule: a price range must not be inverted. */
    public boolean hasValidPriceRange() {
        return minPrice == null || maxPrice == null || minPrice.compareTo(maxPrice) <= 0;
    }
}
