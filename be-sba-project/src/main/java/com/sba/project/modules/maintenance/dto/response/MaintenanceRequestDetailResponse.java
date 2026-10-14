package com.sba.project.modules.maintenance.dto.response;

import com.sba.project.modules.maintenance.enums.ReportRequestPriority;
import com.sba.project.modules.maintenance.enums.ReportRequestStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import tools.jackson.databind.JsonNode;

public record MaintenanceRequestDetailResponse(
        UUID id,
        UUID roomId,
        UUID requesterUserId,
        UUID contractTenantId,
        String title,
        String description,
        ReportRequestPriority priority,
        ReportRequestStatus status,
        UUID assignedTo,
        JsonNode evidenceRefs,
        Instant completedAt,
        Instant slaDueAt,
        Integer rating,
        String ratingComment,
        Instant ratedAt,
        Instant createdAt,
        Instant updatedAt,
        Long version,
        List<FurnitureItemResponse> furnitureItems,
        List<ServiceItemResponse> serviceItems,
        List<MaintenanceRecordResponse> records) {

    public record FurnitureItemResponse(UUID furnitureId, String damageDescription) {
    }

    public record ServiceItemResponse(UUID roomServiceId, String issueDescription) {
    }
}
