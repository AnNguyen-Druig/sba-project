package com.sba.project.modules.maintenance.dto.response;

import com.sba.project.modules.maintenance.enums.ReportRecordApprovalStatus;
import com.sba.project.modules.maintenance.enums.ReportRecordRecordType;
import com.sba.project.modules.maintenance.enums.ReportRecordResponsibleParty;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import tools.jackson.databind.JsonNode;

public record MaintenanceRecordResponse(
        UUID id,
        UUID reportRequestId,
        ReportRecordRecordType recordType,
        UUID parentCostRecordId,
        UUID contractTenantId,
        ReportRecordResponsibleParty responsibleParty,
        BigDecimal amount,
        String description,
        UUID performedBy,
        UUID assignedTo,
        String statusAfter,
        ReportRecordApprovalStatus approvalStatus,
        UUID approvedBy,
        Instant approvedAt,
        JsonNode evidenceRefs,
        Instant occurredAt) {
}
