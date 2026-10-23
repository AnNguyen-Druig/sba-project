package com.sba.project.modules.leasing.dto.request;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

/**
 * P1-57: agreed terms for the draft Contract created from a confirmed Booking. Rent is the agreed price,
 * never defaulted from {@code Room.referencePrice}. {@code firstMonthRent}/{@code lastMonthRent} are the
 * prepaid first/last-period rent of the current schema (see contract-policies.md).
 *
 * @param terms       common Contract terms
 * @param memberTerms terms specific to the booking's customer; empty when not given
 */
public record BookingConversionRequest(
        @NotBlank String terms,
        @NotNull @PositiveOrZero @Digits(integer = 19, fraction = 0) BigDecimal monthlyRent,
        @NotNull @PositiveOrZero @Digits(integer = 19, fraction = 0) BigDecimal firstMonthRent,
        @NotNull @PositiveOrZero @Digits(integer = 19, fraction = 0) BigDecimal lastMonthRent,
        String memberTerms) {
}
