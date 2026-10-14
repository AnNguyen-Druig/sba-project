package com.sba.project.modules.maintenance.service.impl;

import com.sba.project.common.audit.AuditActorContext;
import com.sba.project.common.exceptions.AppException;
import com.sba.project.common.exceptions.ErrorCode;
import com.sba.project.modules.leasing.entity.ContractTenant;
import com.sba.project.modules.leasing.enums.ContractTenantStatus;
import com.sba.project.modules.leasing.repository.ContractTenantHandoverRepository;
import com.sba.project.modules.leasing.repository.TenantDirectoryRepository;
import com.sba.project.modules.maintenance.dto.request.HandoverRequest;
import com.sba.project.modules.maintenance.dto.response.HandoverResponse;
import com.sba.project.modules.maintenance.entity.Furniture;
import com.sba.project.modules.maintenance.repository.FurnitureRepository;
import com.sba.project.modules.maintenance.service.HandoverService;
import com.sba.project.modules.maintenance.service.MaintenanceAccessPolicy;
import java.time.Instant;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

/**
 * No separate handover table exists: the snapshot lives on contract_tenants and history is the audit
 * trail. The server derives asset code/name/status from the furniture rows; only the condition and note
 * come from the caller. A handover can be recorded once per member.
 */
@Service
@RequiredArgsConstructor
public class HandoverServiceImpl implements HandoverService {
    /** DRAFT and CANCELLED memberships never had (or lost) the room, so nothing is handed over. */
    private static final Set<ContractTenantStatus> HANDOVER_STATUSES =
            EnumSet.of(ContractTenantStatus.ACTIVE, ContractTenantStatus.LEFT);

    private final TenantDirectoryRepository tenantRepository;
    private final ContractTenantHandoverRepository handoverRepository;
    private final FurnitureRepository furnitureRepository;
    private final MaintenanceAccessPolicy accessPolicy;
    private final AuditActorContext auditActor;

    @Override
    @Transactional
    public HandoverResponse recordHandover(UUID contractTenantId, HandoverRequest request) {
        UUID actorId = accessPolicy.currentUserId();
        ContractTenant member = loadMember(contractTenantId);
        if (!accessPolicy.canManageRoom(actorId, member.getRoomId())) throw new AppException(ErrorCode.UNAUTHORIZED);
        if (!HANDOVER_STATUSES.contains(member.getStatus()) || member.getHandoverAt() != null) {
            throw new AppException(ErrorCode.MAINTENANCE_STATE_CONFLICT);
        }
        JsonNode snapshot = buildSnapshot(member.getRoomId(), request.items());
        JsonNode evidence = EvidenceReferences.array(request.evidence(), actorId);
        auditActor.bind(actorId);
        if (handoverRepository.record(contractTenantId, actorId, snapshot, evidence, HANDOVER_STATUSES,
                Instant.now()) == 0) {
            throw new AppException(ErrorCode.MAINTENANCE_STATE_CONFLICT);
        }
        return toResponse(loadMember(contractTenantId));
    }

    @Override
    @Transactional(readOnly = true)
    public HandoverResponse getHandover(UUID contractTenantId) {
        UUID actorId = accessPolicy.currentUserId();
        ContractTenant member = loadMember(contractTenantId);
        if (!actorId.equals(member.getTenantUserId()) && !accessPolicy.canManageRoom(actorId, member.getRoomId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        if (member.getHandoverAt() == null) throw new AppException(ErrorCode.RESOURCE_NOT_FOUND);
        return toResponse(member);
    }

    private JsonNode buildSnapshot(UUID roomId, List<HandoverRequest.Item> items) {
        List<UUID> ids = items.stream().map(HandoverRequest.Item::furnitureId).toList();
        if (new HashSet<>(ids).size() != ids.size()) throw new AppException(ErrorCode.MAINTENANCE_DUPLICATE_ITEM);
        Map<UUID, Furniture> furniture = furnitureRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Furniture::getId, Function.identity()));
        if (furniture.size() != ids.size()) throw new AppException(ErrorCode.FURNITURE_NOT_FOUND);
        if (furniture.values().stream().anyMatch(item -> !roomId.equals(item.getRoomId()))) {
            throw new AppException(ErrorCode.MAINTENANCE_ITEM_ROOM_MISMATCH);
        }
        ObjectNode root = JsonNodeFactory.instance.objectNode();
        root.put("recorded_at", Instant.now().toString());
        ArrayNode list = root.putArray("items");
        for (HandoverRequest.Item item : items) {
            Furniture row = furniture.get(item.furnitureId());
            ObjectNode node = list.addObject();
            node.put("furniture_id", row.getId().toString());
            node.put("asset_code", row.getAssetCode());
            node.put("name", row.getName());
            node.put("furniture_status", row.getStatus().name());
            node.put("condition", item.condition().trim());
            if (item.note() != null && !item.note().isBlank()) node.put("note", item.note().trim());
        }
        return root;
    }

    private ContractTenant loadMember(UUID contractTenantId) {
        return tenantRepository.findById(contractTenantId)
                .orElseThrow(() -> new AppException(ErrorCode.CONTRACT_TENANT_NOT_FOUND));
    }

    private static HandoverResponse toResponse(ContractTenant member) {
        return new HandoverResponse(member.getId(), member.getHandoverAt(), member.getHandoverBy(),
                member.getHandoverSnapshot(), member.getHandoverEvidenceRefs());
    }
}
