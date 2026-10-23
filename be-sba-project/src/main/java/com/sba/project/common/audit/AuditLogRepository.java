package com.sba.project.common.audit;

import com.sba.project.common.audit.entity.AuditLog;
import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.Repository;

/** READ-ONLY: rows are written by the database audit_change() trigger and are append-only. */
public interface AuditLogRepository extends Repository<AuditLog, UUID> {
    List<AuditLog> findByTableNameAndEntityIdOrderByOccurredAtAsc(String tableName, UUID entityId);
}
