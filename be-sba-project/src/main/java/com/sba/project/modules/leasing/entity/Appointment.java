package com.sba.project.modules.leasing.entity;

import com.sba.project.common.base.BaseEntity;
import com.sba.project.modules.leasing.enums.AppointmentStatus;
import jakarta.persistence.*;
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

@Entity
@Table(name = "appointments", schema = "rental")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Appointment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "room_id", nullable = false)
    private UUID roomId;

    @Column(name = "requester_user_id", nullable = false)
    private UUID requesterUserId;

    @Column(name = "assigned_to", nullable = true)
    private UUID assignedTo;

    @Column(name = "start_at", nullable = false, columnDefinition = "timestamptz")
    private Instant startAt;

    @Column(name = "end_at", nullable = false, columnDefinition = "timestamptz")
    private Instant endAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AppointmentStatus status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "policy_snapshot", nullable = false, columnDefinition = "jsonb")
    private JsonNode policySnapshot;

    @Column(name = "note", nullable = true, columnDefinition = "text")
    private String note;

    // Scalar revision; no automatic JPA optimistic locking.
    @Builder.Default
    @Column(name = "version", nullable = false)
    private Long version = 0L;
}
