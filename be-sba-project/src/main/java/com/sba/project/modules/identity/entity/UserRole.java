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
@Table(name = "user_roles", schema = "rental")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class UserRole {
    @EmbeddedId
    private UserRoleId id;

    @Column(name = "assigned_by", nullable = true)
    private UUID assignedBy;

    @Column(name = "assigned_at", nullable = false, columnDefinition = "timestamptz", updatable = false)
    private Instant assignedAt;

    @PrePersist
    protected void initializeInsertTimestamps() {
        Instant now = Instant.now();
        if (assignedAt == null) assignedAt = now;
    }

    // Navigation only; assign assignedBy to change the foreign key.
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "assigned_by", referencedColumnName = "id", insertable = false, updatable = false)
    @Setter(lombok.AccessLevel.NONE)
    private User assignedByReference;

    // Navigation only; assign id.roleId to change the foreign key.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", referencedColumnName = "id", insertable = false, updatable = false)
    @Setter(lombok.AccessLevel.NONE)
    private Role roleReference;

    // Navigation only; assign id.userId to change the foreign key.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id", insertable = false, updatable = false)
    @Setter(lombok.AccessLevel.NONE)
    private User userReference;
}
