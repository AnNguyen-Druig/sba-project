package com.sba.project.modules.maintenance.dto.response;

import com.sba.project.modules.maintenance.enums.ReportRequestPriority;
import com.sba.project.modules.maintenance.enums.ReportRequestStatus;
import java.time.Instant;
import java.util.UUID;

public record MaintenanceRequestSummaryResponse(
        UUID id,
        UUID roomId,
        UUID requesterUserId,
        String title,
        ReportRequestPriority priority,
        ReportRequestStatus status,
        UUID assignedTo,
        Instant completedAt,
        Instant createdAt) {
}
