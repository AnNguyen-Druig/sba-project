package com.sba.project.modules.maintenance.entity;

import com.sba.project.modules.maintenance.enums.ReportRecordApprovalStatus;
import com.sba.project.modules.maintenance.enums.ReportRecordRecordType;
import com.sba.project.modules.maintenance.enums.ReportRecordResponsibleParty;
import jakarta.persistence.*;
import java.math.BigDecimal;
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
@Table(name = "report_records", schema = "rental")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ReportRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "report_request_id", nullable = false)
    private UUID reportRequestId;

    @Enumerated(EnumType.STRING)
    @Column(name = "record_type", nullable = false, length = 20)
    private ReportRecordRecordType recordType;

    @Column(name = "parent_cost_record_id", nullable = true)
    private UUID parentCostRecordId;

    @Column(name = "contract_tenant_id", nullable = true)
    private UUID contractTenantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "responsible_party", nullable = true, length = 10)
    private ReportRecordResponsibleParty responsibleParty;

    @Column(name = "amount", nullable = true, precision = 19, scale = 0)
    private BigDecimal amount;

    @Column(name = "description", nullable = false, columnDefinition = "text")
    private String description;

    @Column(name = "performed_by", nullable = false)
    private UUID performedBy;

    @Column(name = "assigned_to", nullable = true)
    private UUID assignedTo;

    @Column(name = "status_after", nullable = true, length = 20)
    private String statusAfter;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = true, length = 20)
    private ReportRecordApprovalStatus approvalStatus;

    @Column(name = "approved_by", nullable = true)
    private UUID approvedBy;

    @Column(name = "approved_at", nullable = true, columnDefinition = "timestamptz")
    private Instant approvedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Builder.Default
    @Column(name = "evidence_refs", nullable = false, columnDefinition = "jsonb")
    private JsonNode evidenceRefs = JsonNodeFactory.instance.arrayNode();

    @Column(name = "occurred_at", nullable = false, columnDefinition = "timestamptz", updatable = false)
    private Instant occurredAt;

    @PrePersist
    protected void initializeInsertTimestamps() {
        Instant now = Instant.now();
        if (occurredAt == null) occurredAt = now;
    }

    // Navigation only; assign parentCostRecordId to change the foreign key.
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "parent_cost_record_id", referencedColumnName = "id", insertable = false, updatable = false)
    @Setter(lombok.AccessLevel.NONE)
    private ReportRecord parentCostRecordReference;

    // Navigation only; assign reportRequestId to change the foreign key.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "report_request_id", referencedColumnName = "id", insertable = false, updatable = false)
    @Setter(lombok.AccessLevel.NONE)
    private ReportRequest reportRequestReference;
}
