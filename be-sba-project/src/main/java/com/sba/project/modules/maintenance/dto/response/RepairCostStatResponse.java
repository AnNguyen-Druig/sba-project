package com.sba.project.modules.maintenance.dto.response;

import com.sba.project.modules.maintenance.enums.ReportRecordResponsibleParty;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * P2-32: approved repair liabilities by room and responsible party. "Cause" is not stored anywhere in
 * the schema, so it is not a grouping dimension.
 */
public record RepairCostStatResponse(
        UUID roomId,
        ReportRecordResponsibleParty responsibleParty,
        BigDecimal totalAmount,
        long liabilityCount) {
}
