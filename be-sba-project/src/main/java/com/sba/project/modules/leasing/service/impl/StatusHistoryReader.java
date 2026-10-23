package com.sba.project.modules.leasing.service.impl;

import com.sba.project.common.audit.AuditLogRepository;
import com.sba.project.common.audit.entity.AuditLog;
import com.sba.project.modules.leasing.dto.response.StatusHistoryResponse;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * Builds a status timeline from the rows written by the database audit_change() trigger, which stores
 * {@code {"before": row, "after": row}} for every insert/update. Updates that keep the status (e.g. a
 * deadline change) are skipped. No history table of our own is needed.
 */
@Component
@RequiredArgsConstructor
class StatusHistoryReader {
    private final AuditLogRepository auditLogRepository;

    List<StatusHistoryResponse> read(String tableName, UUID entityId) {
        return auditLogRepository.findByTableNameAndEntityIdOrderByOccurredAtAsc(tableName, entityId).stream()
                .map(StatusHistoryReader::toStatusChange)
                .flatMap(Optional::stream)
                .toList();
    }

    private static Optional<StatusHistoryResponse> toStatusChange(AuditLog log) {
        String before = status(log.getDetail().path("before"));
        String after = status(log.getDetail().path("after"));
        if (after == null || after.equals(before)) return Optional.empty();
        return Optional.of(new StatusHistoryResponse(before, after, log.getActorUserId(), log.getReason(),
                log.getOccurredAt()));
    }

    private static String status(JsonNode row) {
        JsonNode status = row.path("status");
        return status.isString() ? status.stringValue() : null;
    }
}
