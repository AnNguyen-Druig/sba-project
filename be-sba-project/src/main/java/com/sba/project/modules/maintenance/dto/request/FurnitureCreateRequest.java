package com.sba.project.modules.maintenance.dto.request;

import com.sba.project.modules.maintenance.enums.FurnitureStatus;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** P1-107. Column limits follow rental.furnitures. */
public record FurnitureCreateRequest(
        @NotNull UUID roomId,
        @NotBlank @Size(max = 50) String assetCode,
        @NotBlank String name,
        @Size(max = 100) String furnitureType,
        String serialNumber,
        LocalDate purchaseDate,
        @PositiveOrZero @Digits(integer = 19, fraction = 0) BigDecimal assetValue,
        String condition,
        @NotNull FurnitureStatus status) {
}
