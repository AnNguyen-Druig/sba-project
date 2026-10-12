package com.sba.project.modules.billing.entity;

import com.sba.project.modules.billing.enums.InvoiceItemType;
import com.sba.project.modules.billing.enums.PrepaidBucket;
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
@Table(name = "invoice_items", schema = "rental")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class InvoiceItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "invoice_id", nullable = false)
    private UUID invoiceId;

    @Column(name = "line_number", nullable = false)
    private Integer lineNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 24)
    private InvoiceItemType itemType;

    @Column(name = "description", nullable = false, columnDefinition = "text")
    private String description;

    @Column(name = "quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal unitPrice;

    @Column(name = "amount", nullable = false, precision = 19, scale = 0)
    private BigDecimal amount;

    @Column(name = "source_charge_id", nullable = true)
    private UUID sourceChargeId;

    @Column(name = "source_proposal_id", nullable = true)
    private UUID sourceProposalId;

    @Column(name = "source_report_record_id", nullable = true)
    private UUID sourceReportRecordId;

    @Column(name = "source_payment_id", nullable = true)
    private UUID sourcePaymentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "prepaid_bucket", nullable = true, length = 12)
    private PrepaidBucket prepaidBucket;

    @JdbcTypeCode(SqlTypes.JSON)
    @Builder.Default
    @Column(name = "source_snapshot", nullable = false, columnDefinition = "jsonb")
    private JsonNode sourceSnapshot = JsonNodeFactory.instance.objectNode();

    @Column(name = "idempotency_key", nullable = true, columnDefinition = "text")
    private String idempotencyKey;

    @Column(name = "created_at", nullable = false, columnDefinition = "timestamptz", updatable = false)
    private Instant createdAt;

    @Column(name = "reversed_at", nullable = true, columnDefinition = "timestamptz")
    private Instant reversedAt;

    @Column(name = "reversal_approved_by", nullable = true)
    private UUID reversalApprovedBy;

    @Column(name = "reversal_reason", nullable = true, columnDefinition = "text")
    private String reversalReason;

    @PrePersist
    protected void initializeInsertTimestamps() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
    }

    // Navigation only; assign invoiceId to change the foreign key.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id", referencedColumnName = "id", insertable = false, updatable = false)
    @Setter(lombok.AccessLevel.NONE)
    private Invoice invoiceReference;
}
