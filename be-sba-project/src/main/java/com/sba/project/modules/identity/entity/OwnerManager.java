package com.sba.project.modules.identity.entity;

import jakarta.persistence.*;
import jakarta.persistence.EmbeddedId;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "owner_managers", schema = "rental")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class OwnerManager {
    @EmbeddedId
    private OwnerManagerId id;

    @Column(name = "assigned_at", nullable = false, columnDefinition = "timestamptz", updatable = false)
    private Instant assignedAt;

    @Column(name = "assigned_by", nullable = false)
    private UUID assignedBy;

    @PrePersist
    protected void initializeInsertTimestamps() {
        Instant now = Instant.now();
        if (assignedAt == null) assignedAt = now;
    }

    // Navigation only; assign assignedBy to change the foreign key.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assigned_by", referencedColumnName = "id", insertable = false, updatable = false)
    @Setter(lombok.AccessLevel.NONE)
    private User assignedByReference;

    // Navigation only; assign id.managerId to change the foreign key.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "manager_id", referencedColumnName = "id", insertable = false, updatable = false)
    @Setter(lombok.AccessLevel.NONE)
    private User managerReference;

    // Navigation only; assign id.ownerId to change the foreign key.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", referencedColumnName = "id", insertable = false, updatable = false)
    @Setter(lombok.AccessLevel.NONE)
    private User ownerReference;
}
