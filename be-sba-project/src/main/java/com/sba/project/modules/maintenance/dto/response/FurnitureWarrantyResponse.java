package com.sba.project.modules.maintenance.dto.response;

import java.time.LocalDate;
import java.util.UUID;

public record FurnitureWarrantyResponse(
        UUID id,
        UUID furnitureId,
        String provider,
        LocalDate startDate,
        LocalDate endDate,
        String terms,
        String warrantyCode) {
}
