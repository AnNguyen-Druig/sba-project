package com.sba.project.modules.leasing.entity;

import com.sba.project.modules.leasing.enums.ResourceAllocationAllocationKind;
import com.sba.project.modules.leasing.enums.ResourceAllocationStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "resource_allocations", schema = "rental")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ResourceAllocation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "room_id", nullable = false)
    private UUID roomId;

    @Column(name = "room_slot_id", nullable = true)
    private UUID roomSlotId;

    @Column(name = "booking_id", nullable = true)
    private UUID bookingId;

    @Column(name = "contract_id", nullable = true)
    private UUID contractId;

    @Column(name = "contract_tenant_id", nullable = true)
    private UUID contractTenantId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "expires_at", nullable = true, columnDefinition = "timestamptz")
    private Instant expiresAt;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "allocation_kind", nullable = false, length = 20)
    private ResourceAllocationAllocationKind allocationKind = ResourceAllocationAllocationKind.RENTAL;

    @Column(name = "block_reason", nullable = true, columnDefinition = "text")
    private String blockReason;

    @Column(name = "blocked_by", nullable = true)
    private UUID blockedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ResourceAllocationStatus status;

    @Column(name = "created_at", nullable = false, columnDefinition = "timestamptz", updatable = false)
    private Instant createdAt;

    @Column(name = "released_at", nullable = true, columnDefinition = "timestamptz")
    private Instant releasedAt;

    @PrePersist
    protected void initializeInsertTimestamps() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
    }
}
