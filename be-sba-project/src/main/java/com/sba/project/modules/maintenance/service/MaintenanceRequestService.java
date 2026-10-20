package com.sba.project.modules.maintenance.service;

import com.sba.project.common.base.PagedResponse;
import com.sba.project.modules.maintenance.dto.request.AssignRequest;
import com.sba.project.modules.maintenance.dto.request.CreateMaintenanceRequest;
import com.sba.project.modules.maintenance.dto.request.EvidenceRequest;
import com.sba.project.modules.maintenance.dto.request.TransitionRequest;
import com.sba.project.modules.maintenance.dto.response.MaintenanceRequestDetailResponse;
import com.sba.project.modules.maintenance.dto.response.MaintenanceRequestSummaryResponse;
import com.sba.project.modules.maintenance.enums.ReportRequestPriority;
import com.sba.project.modules.maintenance.enums.ReportRequestStatus;
import com.sba.project.modules.maintenance.dto.request.RatingRequest;
import com.sba.project.modules.maintenance.dto.response.RepairCostStatResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

/** BE-06.4 repair request lifecycle (P1-111..P1-115, P1-119). */
public interface MaintenanceRequestService {
    MaintenanceRequestDetailResponse createRequest(CreateMaintenanceRequest request);

    MaintenanceRequestDetailResponse getRequest(UUID requestId);

    PagedResponse<MaintenanceRequestSummaryResponse> listMyRequests(ReportRequestStatus status, Pageable pageable);

    /** {@code overdueOnly} lists the SLA backlog (P2-30). */
    PagedResponse<MaintenanceRequestSummaryResponse> searchRoomRequests(UUID roomId, ReportRequestStatus status,
            ReportRequestPriority priority, boolean overdueOnly, Pageable pageable);

    /** P2-31: the requester rates a COMPLETED request once. */
    MaintenanceRequestDetailResponse rate(UUID requestId, RatingRequest request);

    /** P2-32: approved repair liabilities per room and party; exactly one of roomId / branchId. */
    List<RepairCostStatResponse> costStats(UUID roomId, UUID branchId);

    MaintenanceRequestDetailResponse accept(UUID requestId, TransitionRequest request);

    MaintenanceRequestDetailResponse reject(UUID requestId, TransitionRequest request);

    MaintenanceRequestDetailResponse start(UUID requestId, TransitionRequest request);

    MaintenanceRequestDetailResponse complete(UUID requestId, TransitionRequest request);

    MaintenanceRequestDetailResponse assign(UUID requestId, AssignRequest request);

    MaintenanceRequestDetailResponse addEvidence(UUID requestId, EvidenceRequest evidence);
}
