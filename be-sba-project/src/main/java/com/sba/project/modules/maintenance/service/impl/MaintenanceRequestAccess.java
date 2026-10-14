package com.sba.project.modules.maintenance.service.impl;

import com.sba.project.common.exceptions.AppException;
import com.sba.project.common.exceptions.ErrorCode;
import com.sba.project.modules.maintenance.entity.ReportRequest;
import com.sba.project.modules.maintenance.repository.ReportRequestRepository;
import com.sba.project.modules.maintenance.service.MaintenanceAccessPolicy;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Loads a repair request and checks the caller's relationship to it (BR-03: role AND relationship). */
@Component
@RequiredArgsConstructor
class MaintenanceRequestAccess {
    private final ReportRequestRepository requestRepository;
    private final MaintenanceAccessPolicy accessPolicy;

    record Participant(ReportRequest request, UUID actorId, boolean manager) {
    }

    /** Owner/Manager of the request's Branch only. */
    Participant requireManager(UUID requestId) {
        Participant participant = requireParticipant(requestId);
        if (!participant.manager()) throw new AppException(ErrorCode.UNAUTHORIZED);
        return participant;
    }

    /** Branch manager or the tenant who filed the request. */
    Participant requireParticipant(UUID requestId) {
        UUID actorId = accessPolicy.currentUserId();
        ReportRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.REPORT_REQUEST_NOT_FOUND));
        boolean manager = accessPolicy.canManageRoom(actorId, request.getRoomId());
        if (!manager && !actorId.equals(request.getRequesterUserId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return new Participant(request, actorId, manager);
    }
}
