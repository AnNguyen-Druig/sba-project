package com.sba.project.modules.maintenance.service.impl;

import com.sba.project.common.audit.AuditActorContext;
import com.sba.project.common.base.PagedResponse;
import com.sba.project.common.exceptions.AppException;
import com.sba.project.common.exceptions.ErrorCode;
import com.sba.project.modules.maintenance.dto.request.AssignRequest;
import com.sba.project.modules.maintenance.dto.request.CreateMaintenanceRequest;
import com.sba.project.modules.maintenance.dto.request.EvidenceRequest;
import com.sba.project.modules.maintenance.dto.request.TransitionRequest;
import com.sba.project.modules.maintenance.dto.response.MaintenanceRequestDetailResponse;
import com.sba.project.modules.maintenance.dto.response.MaintenanceRequestSummaryResponse;
import com.sba.project.modules.maintenance.entity.Furniture;
import com.sba.project.modules.maintenance.entity.ReportRecord;
import com.sba.project.modules.maintenance.entity.ReportRequest;
import com.sba.project.modules.maintenance.entity.ReportRequestFurniture;
import com.sba.project.modules.maintenance.entity.ReportRequestService;
import com.sba.project.modules.maintenance.enums.ReportRecordRecordType;
import com.sba.project.modules.maintenance.enums.ReportRequestPriority;
import com.sba.project.modules.maintenance.enums.ReportRequestStatus;
import com.sba.project.modules.maintenance.mapper.MaintenanceRequestMapper;
import com.sba.project.modules.maintenance.repository.FurnitureRepository;
import com.sba.project.modules.maintenance.repository.ReportRecordRepository;
import com.sba.project.modules.maintenance.repository.ReportRequestFurnitureRepository;
import com.sba.project.modules.maintenance.repository.ReportRequestRepository;
import com.sba.project.modules.maintenance.repository.ReportRequestServiceRepository;
import com.sba.project.modules.maintenance.service.MaintenanceAccessPolicy;
import com.sba.project.modules.maintenance.service.MaintenanceNotifier;
import com.sba.project.modules.maintenance.service.MaintenanceRequestService;
import com.sba.project.modules.maintenance.service.RoomTenancyPort;
import com.sba.project.modules.maintenance.specifications.ReportRequestSpecifications;
import com.sba.project.modules.maintenance.config.MaintenanceProperties;
import com.sba.project.modules.maintenance.dto.request.RatingRequest;
import com.sba.project.modules.maintenance.dto.response.RepairCostStatResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Status actions follow the verbs of P1-114: accept (OPEN->ACCEPTED), reject (OPEN->REJECTED), start
 * (ACCEPTED->IN_PROGRESS), complete (IN_PROGRESS->COMPLETED). The project documents define the status
 * set but no transition graph; CANCELLED has no action here because no feature describes one.
 */
@Service
@RequiredArgsConstructor
public class MaintenanceRequestServiceImpl implements MaintenanceRequestService {
    static final Set<ReportRequestStatus> OPEN_STATUSES = FurnitureServiceImpl.OPEN_REPAIR_STATUSES;
    /** Request states that still accept records and evidence. */
    static final Set<ReportRequestStatus> CLOSED_STATUSES =
            EnumSet.of(ReportRequestStatus.REJECTED, ReportRequestStatus.CANCELLED);
    private static final Set<String> SORTABLE_PROPERTIES = Set.of("createdAt", "priority", "status", "title");
    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "createdAt");

    private final ReportRequestRepository requestRepository;
    private final ReportRequestFurnitureRepository furnitureLinkRepository;
    private final ReportRequestServiceRepository serviceLinkRepository;
    private final ReportRecordRepository recordRepository;
    private final FurnitureRepository furnitureRepository;
    private final MaintenanceRequestMapper mapper;
    private final MaintenanceRequestAccess access;
    private final MaintenanceAccessPolicy accessPolicy;
    private final RoomTenancyPort tenancyPort;
    private final MaintenanceNotifier notifier;
    private final AuditActorContext auditActor;
    private final MaintenanceProperties properties;

    @Override
    @Transactional
    public MaintenanceRequestDetailResponse createRequest(CreateMaintenanceRequest command) {
        UUID actorId = accessPolicy.currentUserId();
        List<CreateMaintenanceRequest.FurnitureItem> furnitureItems = nullToEmpty(command.furnitureItems());
        List<CreateMaintenanceRequest.ServiceItem> serviceItems = nullToEmpty(command.serviceItems());
        if (furnitureItems.isEmpty() && serviceItems.isEmpty()) {
            throw new AppException(ErrorCode.MAINTENANCE_ITEM_REQUIRED);
        }
        UUID contractTenantId = tenancyPort.findActiveContractTenantId(actorId, command.roomId())
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
        validateFurniture(command.roomId(), furnitureItems);
        requireDistinct(serviceItems.stream().map(CreateMaintenanceRequest.ServiceItem::roomServiceId).toList());

        ReportRequest request = requestRepository.save(ReportRequest.builder()
                .roomId(command.roomId()).requesterUserId(actorId).contractTenantId(contractTenantId)
                .title(command.title().trim()).description(command.description().trim())
                .priority(command.priority()).status(ReportRequestStatus.OPEN).slaDueAt(slaDeadline(command.priority()))
                .evidenceRefs(EvidenceReferences.array(command.evidence(), actorId)).build());
        furnitureLinkRepository.saveAll(furnitureItems.stream().map(item -> ReportRequestFurniture.builder()
                .reportRequestId(request.getId()).roomId(request.getRoomId()).furnitureId(item.furnitureId())
                .damageDescription(item.damageDescription()).build()).toList());
        // room_services must belong to the room: enforced by the composite FK (409 on violation).
        serviceLinkRepository.saveAll(serviceItems.stream().map(item -> ReportRequestService.builder()
                .reportRequestId(request.getId()).roomId(request.getRoomId()).roomServiceId(item.roomServiceId())
                .issueDescription(item.issueDescription()).build()).toList());
        return detail(request, true);
    }

    @Override
    @Transactional(readOnly = true)
    public MaintenanceRequestDetailResponse getRequest(UUID requestId) {
        MaintenanceRequestAccess.Participant participant = access.requireParticipant(requestId);
        return detail(participant.request(), participant.manager());
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<MaintenanceRequestSummaryResponse> listMyRequests(ReportRequestStatus status,
            Pageable pageable) {
        UUID actorId = accessPolicy.currentUserId();
        return page(ReportRequestSpecifications.forRequester(actorId, status), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<MaintenanceRequestSummaryResponse> searchRoomRequests(UUID roomId,
            ReportRequestStatus status, ReportRequestPriority priority, boolean overdueOnly, Pageable pageable) {
        if (!accessPolicy.canManageRoom(accessPolicy.currentUserId(), roomId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return page(ReportRequestSpecifications.forRoom(roomId, status, priority, overdueOnly, Instant.now()),
                pageable);
    }

    @Override
    @Transactional
    public MaintenanceRequestDetailResponse rate(UUID requestId, RatingRequest command) {
        UUID actorId = accessPolicy.currentUserId();
        ReportRequest request = reload(requestId);
        if (!actorId.equals(request.getRequesterUserId())) throw new AppException(ErrorCode.UNAUTHORIZED);
        if (request.getStatus() != ReportRequestStatus.COMPLETED || request.getRating() != null) {
            throw new AppException(ErrorCode.MAINTENANCE_STATE_CONFLICT);
        }
        String comment = command.comment() == null || command.comment().isBlank() ? null : command.comment().trim();
        if (requestRepository.rate(requestId, actorId, command.rating(), comment, Instant.now()) == 0) {
            throw new AppException(ErrorCode.MAINTENANCE_STATE_CONFLICT);
        }
        return detail(reload(requestId), false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RepairCostStatResponse> costStats(UUID roomId, UUID branchId) {
        UUID actorId = accessPolicy.currentUserId();
        if ((roomId == null) == (branchId == null)) throw new AppException(ErrorCode.INVALID_REQUEST);
        if (roomId != null) {
            if (!accessPolicy.canManageRoom(actorId, roomId)) throw new AppException(ErrorCode.UNAUTHORIZED);
            return recordRepository.costStatsByRoom(roomId);
        }
        if (!accessPolicy.canManageBranch(actorId, branchId)) throw new AppException(ErrorCode.UNAUTHORIZED);
        return recordRepository.costStatsByBranch(branchId);
    }

    /** Deadline from configuration (P2-30); null when the priority has no configured SLA. */
    private Instant slaDeadline(ReportRequestPriority priority) {
        int hours = properties.slaHoursFor(priority);
        return hours > 0 ? Instant.now().plus(Duration.ofHours(hours)) : null;
    }

    @Override
    @Transactional
    public MaintenanceRequestDetailResponse accept(UUID requestId, TransitionRequest request) {
        return transition(requestId, ReportRequestStatus.OPEN, ReportRequestStatus.ACCEPTED, request);
    }

    @Override
    @Transactional
    public MaintenanceRequestDetailResponse reject(UUID requestId, TransitionRequest request) {
        return transition(requestId, ReportRequestStatus.OPEN, ReportRequestStatus.REJECTED, request);
    }

    @Override
    @Transactional
    public MaintenanceRequestDetailResponse start(UUID requestId, TransitionRequest request) {
        return transition(requestId, ReportRequestStatus.ACCEPTED, ReportRequestStatus.IN_PROGRESS, request);
    }

    @Override
    @Transactional
    public MaintenanceRequestDetailResponse complete(UUID requestId, TransitionRequest request) {
        return transition(requestId, ReportRequestStatus.IN_PROGRESS, ReportRequestStatus.COMPLETED, request);
    }

    @Override
    @Transactional
    public MaintenanceRequestDetailResponse assign(UUID requestId, AssignRequest command) {
        MaintenanceRequestAccess.Participant manager = access.requireManager(requestId);
        ReportRequest request = manager.request();
        if (!OPEN_STATUSES.contains(request.getStatus())) {
            throw new AppException(ErrorCode.MAINTENANCE_STATE_CONFLICT);
        }
        if (!accessPolicy.canManageRoom(command.assigneeUserId(), request.getRoomId())) {
            throw new AppException(ErrorCode.MAINTENANCE_INVALID_ASSIGNEE);
        }
        auditActor.bind(manager.actorId());
        // The ASSIGNMENT record is the assignment history (D31); the request keeps the current assignee.
        Instant now = Instant.now();
        if (requestRepository.assign(requestId, command.assigneeUserId(), request.getVersion(),
                OPEN_STATUSES, now) == 0) {
            throw new AppException(ErrorCode.CONCURRENT_MODIFICATION);
        }
        recordRepository.save(ReportRecord.builder().reportRequestId(requestId)
                .recordType(ReportRecordRecordType.ASSIGNMENT).description("Assigned main handler")
                .performedBy(manager.actorId()).assignedTo(command.assigneeUserId()).build());
        return detail(reload(requestId), true);
    }

    @Override
    @Transactional
    public MaintenanceRequestDetailResponse addEvidence(UUID requestId, EvidenceRequest evidence) {
        MaintenanceRequestAccess.Participant participant = access.requireParticipant(requestId);
        if (CLOSED_STATUSES.contains(participant.request().getStatus())) {
            throw new AppException(ErrorCode.MAINTENANCE_STATE_CONFLICT);
        }
        String reference = EvidenceReferences.entry(evidence, participant.actorId()).toString();
        if (requestRepository.appendEvidence(requestId, reference) == 0) {
            throw new AppException(ErrorCode.REPORT_REQUEST_NOT_FOUND);
        }
        return detail(reload(requestId), participant.manager());
    }

    private MaintenanceRequestDetailResponse transition(UUID requestId, ReportRequestStatus from,
            ReportRequestStatus to, TransitionRequest command) {
        MaintenanceRequestAccess.Participant manager = access.requireManager(requestId);
        ReportRequest request = manager.request();
        if (request.getStatus() != from) throw new AppException(ErrorCode.MAINTENANCE_STATE_CONFLICT);
        auditActor.bind(manager.actorId());
        Instant now = Instant.now();
        int updated = to == ReportRequestStatus.COMPLETED
                ? requestRepository.complete(requestId, from, to, request.getVersion(), now)
                : requestRepository.transition(requestId, from, to, request.getVersion(), now);
        if (updated == 0) throw new AppException(ErrorCode.CONCURRENT_MODIFICATION);
        String note = command == null || command.note() == null || command.note().isBlank()
                ? "Status changed to " + to.name() : command.note().trim();
        recordRepository.save(ReportRecord.builder().reportRequestId(requestId)
                .recordType(ReportRecordRecordType.STATUS).description(note)
                .performedBy(manager.actorId()).statusAfter(to.name()).build());
        // One stable type per status: the notification layer de-duplicates by type + reference (P1-126).
        notifier.notifyUser(request.getRequesterUserId(), "REPAIR_REQUEST_" + to.name(),
                "Repair request " + to.name().toLowerCase().replace('_', ' '), request.getTitle(), requestId);
        return detail(reload(requestId), true);
    }

    private ReportRequest reload(UUID requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.REPORT_REQUEST_NOT_FOUND));
    }

    private void validateFurniture(UUID roomId, List<CreateMaintenanceRequest.FurnitureItem> items) {
        List<UUID> ids = items.stream().map(CreateMaintenanceRequest.FurnitureItem::furnitureId).toList();
        requireDistinct(ids);
        if (ids.isEmpty()) return;
        List<Furniture> found = furnitureRepository.findAllById(ids);
        if (found.size() != ids.size()) throw new AppException(ErrorCode.FURNITURE_NOT_FOUND);
        if (found.stream().anyMatch(furniture -> !roomId.equals(furniture.getRoomId()))) {
            throw new AppException(ErrorCode.MAINTENANCE_ITEM_ROOM_MISMATCH);
        }
    }

    private static void requireDistinct(List<UUID> ids) {
        if (new HashSet<>(ids).size() != ids.size()) throw new AppException(ErrorCode.MAINTENANCE_DUPLICATE_ITEM);
    }

    private static <T> List<T> nullToEmpty(List<T> list) {
        return list == null ? List.of() : list;
    }

    /** Tenants never see COST/LIABILITY records (owner-side accounting); managers see everything. */
    private MaintenanceRequestDetailResponse detail(ReportRequest request, boolean manager) {
        List<ReportRecord> records = recordRepository.findByReportRequestIdOrderByOccurredAtAsc(request.getId())
                .stream().filter(record -> manager || record.getRecordType() != ReportRecordRecordType.COST
                        && record.getRecordType() != ReportRecordRecordType.LIABILITY)
                .collect(Collectors.toList());
        return mapper.toDetail(request, furnitureLinkRepository.findByReportRequestId(request.getId()),
                serviceLinkRepository.findByReportRequestId(request.getId()), records);
    }

    private PagedResponse<MaintenanceRequestSummaryResponse> page(
            org.springframework.data.jpa.domain.Specification<ReportRequest> specification, Pageable pageable) {
        Page<ReportRequest> page = requestRepository.findAll(specification, withAllowedSort(pageable));
        return PagedResponse.of(page.getContent().stream().map(mapper::toSummary).toList(),
                page.getNumber() + 1, page.getSize(), page.getTotalElements());
    }

    private Pageable withAllowedSort(Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), DEFAULT_SORT);
        }
        for (Sort.Order order : pageable.getSort()) {
            if (!SORTABLE_PROPERTIES.contains(order.getProperty())) throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        return pageable;
    }
}
