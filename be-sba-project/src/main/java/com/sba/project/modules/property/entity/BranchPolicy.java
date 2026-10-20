package com.sba.project.modules.property.entity;

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
@Table(name = "branch_policies", schema = "rental")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class BranchPolicy {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "effective_from", nullable = false, columnDefinition = "timestamptz")
    private Instant effectiveFrom;

    @Column(name = "collection_day", nullable = false)
    private Integer collectionDay;

    @Column(name = "payment_grace_days", nullable = false)
    private Integer paymentGraceDays;

    @Column(name = "booking_hold_amount", nullable = false, precision = 19, scale = 0)
    private BigDecimal bookingHoldAmount;

    @Column(name = "payment_window_minutes", nullable = false)
    private Integer paymentWindowMinutes;

    @Column(name = "temporary_hold_minutes", nullable = false)
    private Integer temporaryHoldMinutes;

    @Column(name = "confirmed_hold_minutes", nullable = false)
    private Integer confirmedHoldMinutes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Builder.Default
    @Column(name = "appointment_policy", nullable = false, columnDefinition = "jsonb")
    private JsonNode appointmentPolicy = JsonNodeFactory.instance.objectNode();

    @Column(name = "cancellation_terms", nullable = true, columnDefinition = "text")
    private String cancellationTerms;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false, columnDefinition = "timestamptz", updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void initializeInsertTimestamps() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
    }

    // Navigation only; assign branchId to change the foreign key.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", referencedColumnName = "id", insertable = false, updatable = false)
    @Setter(lombok.AccessLevel.NONE)
    private Branch branchReference;
}
