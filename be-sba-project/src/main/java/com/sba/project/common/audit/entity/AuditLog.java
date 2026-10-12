package com.sba.project.common.audit.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import tools.jackson.databind.JsonNode;

@Entity
@Table(name = "audit_logs", schema = "rental")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Immutable
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "actor_user_id", nullable = true)
    private UUID actorUserId;

    @Column(name = "table_name", nullable = false, columnDefinition = "text")
    private String tableName;

    @Column(name = "entity_id", nullable = true)
    private UUID entityId;

    @Column(name = "action", nullable = false, columnDefinition = "text")
    private String action;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "detail", nullable = false, columnDefinition = "jsonb")
    private JsonNode detail;

    @Column(name = "reason", nullable = true, columnDefinition = "text")
    private String reason;

    @Column(name = "request_key", nullable = true, columnDefinition = "text")
    private String requestKey;

    @Column(name = "occurred_at", nullable = false, columnDefinition = "timestamptz", updatable = false)
    private Instant occurredAt;

    @PrePersist
    protected void initializeInsertTimestamps() {
        Instant now = Instant.now();
        if (occurredAt == null) occurredAt = now;
    }
}
