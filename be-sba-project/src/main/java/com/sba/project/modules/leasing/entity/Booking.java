package com.sba.project.modules.leasing.entity;

import com.sba.project.common.base.BaseEntity;
import com.sba.project.modules.leasing.enums.BookingStatus;
import com.sba.project.modules.leasing.enums.PaymentEnvironment;
import com.sba.project.modules.leasing.enums.RentalMode;
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
@Table(name = "bookings", schema = "rental")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Booking extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    @Column(name = "room_id", nullable = false)
    private UUID roomId;

    @Column(name = "room_slot_id", nullable = true)
    private UUID roomSlotId;

    @Column(name = "requester_user_id", nullable = false)
    private UUID requesterUserId;

    @Column(name = "appointment_id", nullable = true)
    private UUID appointmentId;

    @Column(name = "policy_id", nullable = false)
    private UUID policyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "rental_mode", nullable = false, length = 4)
    private RentalMode rentalMode;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "hold_amount", nullable = false, precision = 19, scale = 0)
    private BigDecimal holdAmount;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "policy_snapshot", nullable = false, columnDefinition = "jsonb")
    private JsonNode policySnapshot;

    @Column(name = "payment_due_at", nullable = false, columnDefinition = "timestamptz")
    private Instant paymentDueAt;

    @Column(name = "temporary_hold_until", nullable = false, columnDefinition = "timestamptz")
    private Instant temporaryHoldUntil;

    @Column(name = "confirmed_hold_until", nullable = true, columnDefinition = "timestamptz")
    private Instant confirmedHoldUntil;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_environment", nullable = false, length = 10)
    private PaymentEnvironment paymentEnvironment;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 24)
    private BookingStatus status;

    @Column(name = "cancellation_reason", nullable = true, columnDefinition = "text")
    private String cancellationReason;

    // Scalar revision; no automatic JPA optimistic locking.
    @Builder.Default
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    // Navigation only; assign appointmentId to change the foreign key.
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "appointment_id", referencedColumnName = "id", insertable = false, updatable = false)
    @Setter(lombok.AccessLevel.NONE)
    private Appointment appointmentReference;
}
