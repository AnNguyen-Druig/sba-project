package com.sba.project.modules.leasing.entity;

import com.sba.project.common.base.BaseEntity;
import com.sba.project.modules.leasing.enums.MoveOutStatus;
import com.sba.project.modules.leasing.enums.PrepaidLastDisposition;
import com.sba.project.modules.leasing.enums.ContractTenantRole;
import com.sba.project.modules.leasing.enums.ContractTenantStatus;
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
import tools.jackson.databind.node.JsonNodeFactory;

@Entity
@Table(name = "contract_tenants", schema = "rental")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ContractTenant extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "contract_id", nullable = false)
    private UUID contractId;

    @Column(name = "tenant_user_id", nullable = false)
    private UUID tenantUserId;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "room_id", nullable = false)
    private UUID roomId;

    @Column(name = "room_slot_id", nullable = true)
    private UUID roomSlotId;

    @Column(name = "source_booking_id", nullable = true)
    private UUID sourceBookingId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tenant_snapshot", nullable = false, columnDefinition = "jsonb")
    private JsonNode tenantSnapshot;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "actual_leave_date", nullable = true)
    private LocalDate actualLeaveDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private ContractTenantRole role;

    @Column(name = "monthly_rent", nullable = false, precision = 19, scale = 0)
    private BigDecimal monthlyRent;

    @Column(name = "first_month_rent", nullable = false, precision = 19, scale = 0)
    private BigDecimal firstMonthRent;

    @Column(name = "last_month_rent", nullable = false, precision = 19, scale = 0)
    private BigDecimal lastMonthRent;

    @Column(name = "prepaid_first_required", nullable = false, precision = 19, scale = 0)
    private BigDecimal prepaidFirstRequired;

    @Column(name = "prepaid_last_required", nullable = false, precision = 19, scale = 0)
    private BigDecimal prepaidLastRequired;

    @Column(name = "terms", nullable = false, columnDefinition = "text")
    private String terms;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ContractTenantStatus status;

    // Scalar revision; no automatic JPA optimistic locking.
    @Builder.Default
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @Column(name = "move_out_requested_at", nullable = true, columnDefinition = "timestamptz")
    private Instant moveOutRequestedAt;

    @Column(name = "requested_leave_date", nullable = true)
    private LocalDate requestedLeaveDate;

    @Column(name = "move_out_reason", nullable = true, columnDefinition = "text")
    private String moveOutReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "move_out_status", nullable = true, length = 20)
    private MoveOutStatus moveOutStatus;

    @Column(name = "move_out_processed_by", nullable = true)
    private UUID moveOutProcessedBy;

    @Column(name = "move_out_processed_at", nullable = true, columnDefinition = "timestamptz")
    private Instant moveOutProcessedAt;

    @Column(name = "handover_at", nullable = true, columnDefinition = "timestamptz")
    private Instant handoverAt;

    @Column(name = "handover_by", nullable = true)
    private UUID handoverBy;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "handover_snapshot", nullable = true, columnDefinition = "jsonb")
    private JsonNode handoverSnapshot;

    @JdbcTypeCode(SqlTypes.JSON)
    @Builder.Default
    @Column(name = "handover_evidence_refs", nullable = false, columnDefinition = "jsonb")
    private JsonNode handoverEvidenceRefs = JsonNodeFactory.instance.arrayNode();

    @Enumerated(EnumType.STRING)
    @Column(name = "prepaid_last_disposition", nullable = true, length = 20)
    private PrepaidLastDisposition prepaidLastDisposition;

    @Column(name = "disposition_amount", nullable = true, precision = 19, scale = 0)
    private BigDecimal dispositionAmount;

    @Column(name = "disposition_reason", nullable = true, columnDefinition = "text")
    private String dispositionReason;

    @Column(name = "disposition_approved_by", nullable = true)
    private UUID dispositionApprovedBy;

    @Column(name = "disposition_approved_at", nullable = true, columnDefinition = "timestamptz")
    private Instant dispositionApprovedAt;
}
