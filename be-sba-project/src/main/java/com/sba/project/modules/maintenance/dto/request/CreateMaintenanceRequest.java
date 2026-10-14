package com.sba.project.modules.maintenance.dto.request;

import com.sba.project.modules.maintenance.enums.ReportRequestPriority;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

/** P1-111/P1-112. At least one furniture or service item is required (Furniture OR Service). */
public record CreateMaintenanceRequest(
        @NotNull UUID roomId,
        @NotBlank String title,
        @NotBlank String description,
        @NotNull ReportRequestPriority priority,
        @Valid List<FurnitureItem> furnitureItems,
        @Valid List<ServiceItem> serviceItems,
        @Valid List<EvidenceRequest> evidence) {

    public record FurnitureItem(@NotNull UUID furnitureId, String damageDescription) {
    }

    public record ServiceItem(@NotNull UUID roomServiceId, String issueDescription) {
    }
}
