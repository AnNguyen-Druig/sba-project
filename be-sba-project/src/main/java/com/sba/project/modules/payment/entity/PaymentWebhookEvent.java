package com.sba.project.modules.payment.entity;

import com.sba.project.modules.payment.enums.WebhookEnvironment;
import com.sba.project.modules.payment.enums.PaymentWebhookEventProcessingStatus;
import com.sba.project.modules.payment.enums.PaymentWebhookEventProvider;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import tools.jackson.databind.JsonNode;

@Entity
@Table(name = "payment_webhook_events", schema = "rental")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class PaymentWebhookEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "payment_id", nullable = true)
    private UUID paymentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 10)
    private PaymentWebhookEventProvider provider;

    @Enumerated(EnumType.STRING)
    @Column(name = "environment", nullable = false, length = 10)
    private WebhookEnvironment environment;

    @Column(name = "provider_account_key", nullable = false, columnDefinition = "text")
    private String providerAccountKey;

    @Column(name = "event_key", nullable = false, columnDefinition = "text")
    private String eventKey;

    @Column(name = "payload_hash", nullable = false, columnDefinition = "text")
    private String payloadHash;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "safe_payload", nullable = false, columnDefinition = "jsonb")
    private JsonNode safePayload;

    @Column(name = "signature_verified", nullable = false)
    private Boolean signatureVerified;

    @Enumerated(EnumType.STRING)
    @Column(name = "processing_status", nullable = false, length = 20)
    private PaymentWebhookEventProcessingStatus processingStatus;

    @Column(name = "received_at", nullable = false, columnDefinition = "timestamptz", updatable = false)
    private Instant receivedAt;

    @Column(name = "processed_at", nullable = true, columnDefinition = "timestamptz")
    private Instant processedAt;

    @PrePersist
    protected void initializeInsertTimestamps() {
        Instant now = Instant.now();
        if (receivedAt == null) receivedAt = now;
    }

    // Navigation only; assign paymentId to change the foreign key.
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "payment_id", referencedColumnName = "id", insertable = false, updatable = false)
    @Setter(lombok.AccessLevel.NONE)
    private Payment paymentReference;
}
