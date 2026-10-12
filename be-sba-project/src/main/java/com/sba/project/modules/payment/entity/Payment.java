package com.sba.project.modules.payment.entity;

import com.sba.project.modules.payment.enums.PaymentEnvironment;
import com.sba.project.modules.payment.enums.PaymentMethod;
import com.sba.project.modules.payment.enums.PaymentStatus;
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
@Table(name = "payments", schema = "rental")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    @Column(name = "invoice_id", nullable = true)
    private UUID invoiceId;

    @Column(name = "booking_id", nullable = true)
    private UUID bookingId;

    @Column(name = "payer_user_id", nullable = true)
    private UUID payerUserId;

    @Column(name = "payer_name_snapshot", nullable = true, columnDefinition = "text")
    private String payerNameSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false, length = 20)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(name = "environment", nullable = false, length = 10)
    private PaymentEnvironment environment;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "amount_received", nullable = false, precision = 19, scale = 0)
    private BigDecimal amountReceived;

    @Builder.Default
    @Column(name = "allocated_amount", nullable = false, precision = 19, scale = 0)
    private BigDecimal allocatedAmount = new BigDecimal("0");

    @Column(name = "prepaid_contract_tenant_id", nullable = true)
    private UUID prepaidContractTenantId;

    @Builder.Default
    @Column(name = "prepaid_first_amount", nullable = false, precision = 19, scale = 0)
    private BigDecimal prepaidFirstAmount = new BigDecimal("0");

    @Builder.Default
    @Column(name = "prepaid_last_amount", nullable = false, precision = 19, scale = 0)
    private BigDecimal prepaidLastAmount = new BigDecimal("0");

    @Builder.Default
    @Column(name = "unallocated_refunded", nullable = false, precision = 19, scale = 0)
    private BigDecimal unallocatedRefunded = new BigDecimal("0");

    @Builder.Default
    @Column(name = "prepaid_first_refunded", nullable = false, precision = 19, scale = 0)
    private BigDecimal prepaidFirstRefunded = new BigDecimal("0");

    @Builder.Default
    @Column(name = "prepaid_last_refunded", nullable = false, precision = 19, scale = 0)
    private BigDecimal prepaidLastRefunded = new BigDecimal("0");

    @Column(name = "provider_account_key", nullable = true, columnDefinition = "text")
    private String providerAccountKey;

    @Column(name = "provider_transaction_id", nullable = true, columnDefinition = "text")
    private String providerTransactionId;

    @Column(name = "idempotency_key", nullable = false, columnDefinition = "text")
    private String idempotencyKey;

    @Column(name = "reference_text", nullable = true, columnDefinition = "text")
    private String referenceText;

    @JdbcTypeCode(SqlTypes.JSON)
    @Builder.Default
    @Column(name = "evidence_refs", nullable = false, columnDefinition = "jsonb")
    private JsonNode evidenceRefs = JsonNodeFactory.instance.arrayNode();

    @Column(name = "received_at", nullable = true, columnDefinition = "timestamptz")
    private Instant receivedAt;

    @Column(name = "verified_by", nullable = true)
    private UUID verifiedBy;

    @Column(name = "verified_at", nullable = true, columnDefinition = "timestamptz")
    private Instant verifiedAt;

    @Column(name = "verification_note", nullable = true, columnDefinition = "text")
    private String verificationNote;

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
}
