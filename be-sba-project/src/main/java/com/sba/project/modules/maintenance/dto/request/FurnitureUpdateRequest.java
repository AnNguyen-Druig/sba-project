package com.sba.project.modules.maintenance.dto.request;

import com.sba.project.modules.maintenance.enums.FurnitureStatus;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Full replacement (PUT). No roomId: moving an asset would break repair links (P1-110). */
public record FurnitureUpdateRequest(
        @NotBlank @Size(max = 50) String assetCode,
        @NotBlank String name,
        @Size(max = 100) String furnitureType,
        String serialNumber,
        LocalDate purchaseDate,
        @PositiveOrZero @Digits(integer = 19, fraction = 0) BigDecimal assetValue,
        String condition,
        @NotNull FurnitureStatus status) {
}
