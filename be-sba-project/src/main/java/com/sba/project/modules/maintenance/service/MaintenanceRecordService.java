package com.sba.project.modules.maintenance.service;

import com.sba.project.modules.maintenance.dto.request.LiabilityRequest;
import com.sba.project.modules.maintenance.dto.request.MaintenanceRecordRequest;
import com.sba.project.modules.maintenance.dto.response.MaintenanceRecordResponse;
import java.util.UUID;

/** BE-06.4 processing records, cost and liability (P1-116..P1-118). Owner/Manager of the Branch only. */
public interface MaintenanceRecordService {
    MaintenanceRecordResponse addRecord(UUID requestId, MaintenanceRecordRequest request);

    MaintenanceRecordResponse addLiability(UUID requestId, UUID costRecordId, LiabilityRequest request);

    MaintenanceRecordResponse approveRecord(UUID requestId, UUID recordId);

    MaintenanceRecordResponse rejectRecord(UUID requestId, UUID recordId);
}
