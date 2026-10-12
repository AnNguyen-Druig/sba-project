package com.sba.project.modules.maintenance.entity;

import com.sba.project.common.base.BaseEntity;
import com.sba.project.modules.maintenance.enums.ReportRequestPriority;
import com.sba.project.modules.maintenance.enums.ReportRequestStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;

@Entity
@Table(name = "report_requests", schema = "rental")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ReportRequest extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "room_id", nullable = false)
    private UUID roomId;

    @Column(name = "requester_user_id", nullable = false)
    private UUID requesterUserId;

    @Column(name = "contract_tenant_id", nullable = true)
    private UUID contractTenantId;

    @Column(name = "title", nullable = false, columnDefinition = "text")
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 10)
    private ReportRequestPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReportRequestStatus status;

    @Column(name = "assigned_to", nullable = true)
    private UUID assignedTo;

    @JdbcTypeCode(SqlTypes.JSON)
    @Builder.Default
    @Column(name = "evidence_refs", nullable = false, columnDefinition = "jsonb")
    private JsonNode evidenceRefs = JsonNodeFactory.instance.arrayNode();

    @Column(name = "completed_at", nullable = true, columnDefinition = "timestamptz")
    private Instant completedAt;

    // Scalar revision; no automatic JPA optimistic locking.
    @Builder.Default
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @Column(name = "sla_due_at", nullable = true, columnDefinition = "timestamptz")
    private Instant slaDueAt;

    @Column(name = "rating", nullable = true)
    private Integer rating;

    @Column(name = "rating_comment", nullable = true, columnDefinition = "text")
    private String ratingComment;

    @Column(name = "rated_at", nullable = true, columnDefinition = "timestamptz")
    private Instant ratedAt;
}
