package com.sba.project.modules.utilities.entity;

import com.sba.project.modules.utilities.enums.AllocationMethod;
import com.sba.project.modules.utilities.enums.CostAllocationProposalStatus;
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
@Table(name = "cost_allocation_proposals", schema = "rental")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class CostAllocationProposal {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "room_service_charge_id", nullable = false)
    private UUID roomServiceChargeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false, length = 20)
    private AllocationMethod method;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "allocation_details", nullable = false, columnDefinition = "jsonb")
    private JsonNode allocationDetails;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "calculation_snapshot", nullable = false, columnDefinition = "jsonb")
    private JsonNode calculationSnapshot;

    @Column(name = "reason", nullable = true, columnDefinition = "text")
    private String reason;

    @Column(name = "proposed_by", nullable = true)
    private UUID proposedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CostAllocationProposalStatus status;

    @Column(name = "processed_by", nullable = true)
    private UUID processedBy;

    @Column(name = "processed_at", nullable = true, columnDefinition = "timestamptz")
    private Instant processedAt;

    @Column(name = "created_at", nullable = false, columnDefinition = "timestamptz", updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void initializeInsertTimestamps() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
    }

    // Navigation only; assign roomServiceChargeId to change the foreign key.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_service_charge_id", referencedColumnName = "id", insertable = false, updatable = false)
    @Setter(lombok.AccessLevel.NONE)
    private RoomServiceCharge roomServiceChargeReference;
}
