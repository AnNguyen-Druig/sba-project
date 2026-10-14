package com.sba.project.modules.maintenance.dto.response;

import com.sba.project.modules.maintenance.enums.FurnitureStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record FurnitureResponse(
        UUID id,
        UUID roomId,
        String assetCode,
        String name,
        String furnitureType,
        String serialNumber,
        LocalDate purchaseDate,
        BigDecimal assetValue,
        String condition,
        FurnitureStatus status) {
}
