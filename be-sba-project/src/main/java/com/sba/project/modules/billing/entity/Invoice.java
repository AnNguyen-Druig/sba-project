package com.sba.project.modules.billing.entity;

import com.sba.project.modules.billing.enums.InvoiceDocumentStatus;
import com.sba.project.modules.billing.enums.PaymentEnvironment;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import tools.jackson.databind.JsonNode;

@Entity
@Table(name = "invoices", schema = "rental")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "contract_tenant_id", nullable = false)
    private UUID contractTenantId;

    @Column(name = "billing_month", nullable = false)
    private LocalDate billingMonth;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "policy_snapshot", nullable = false, columnDefinition = "jsonb")
    private JsonNode policySnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_status", nullable = false, length = 12)
    private InvoiceDocumentStatus documentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_environment", nullable = false, length = 10)
    private PaymentEnvironment paymentEnvironment;

    @Column(name = "subtotal_amount", nullable = false, precision = 19, scale = 0)
    private BigDecimal subtotalAmount;

    @Column(name = "adjustment_amount", nullable = false, precision = 19, scale = 0)
    private BigDecimal adjustmentAmount;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 0)
    private BigDecimal totalAmount;

    @Column(name = "replaces_invoice_id", nullable = true)
    private UUID replacesInvoiceId;

    @Column(name = "issued_by", nullable = true)
    private UUID issuedBy;

    @Column(name = "issued_at", nullable = true, columnDefinition = "timestamptz")
    private Instant issuedAt;

    @Column(name = "cancellation_reason", nullable = true, columnDefinition = "text")
    private String cancellationReason;

    @Column(name = "adjustment_approved_by", nullable = true)
    private UUID adjustmentApprovedBy;

    @Column(name = "adjustment_approved_at", nullable = true, columnDefinition = "timestamptz")
    private Instant adjustmentApprovedAt;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false, columnDefinition = "timestamptz", updatable = false)
    private Instant createdAt;

    // Scalar revision; no automatic JPA optimistic locking.
    @Builder.Default
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @PrePersist
    protected void initializeInsertTimestamps() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
    }

    // Navigation only; assign replacesInvoiceId to change the foreign key.
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "replaces_invoice_id", referencedColumnName = "id", insertable = false, updatable = false)
    @Setter(lombok.AccessLevel.NONE)
    private Invoice replacesInvoiceReference;
}
