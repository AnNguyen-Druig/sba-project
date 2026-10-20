package com.sba.project.modules.utilities.entity;

import com.sba.project.modules.utilities.enums.ChargeSegmentReason;
import com.sba.project.modules.utilities.enums.RoomServiceChargeStatus;
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
@Table(name = "room_service_charges", schema = "rental")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class RoomServiceCharge {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "room_service_id", nullable = false)
    private UUID roomServiceId;

    @Column(name = "room_id", nullable = false)
    private UUID roomId;

    @Column(name = "billing_month", nullable = false)
    private LocalDate billingMonth;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "segment_number", nullable = false)
    private Integer segmentNumber;

    @Column(name = "meter_reference", nullable = true, columnDefinition = "text")
    private String meterReference;

    @Enumerated(EnumType.STRING)
    @Column(name = "segment_reason", nullable = false, length = 20)
    private ChargeSegmentReason segmentReason;

    @Column(name = "start_reading", nullable = true, precision = 19, scale = 4)
    private BigDecimal startReading;

    @Column(name = "end_reading", nullable = true, precision = 19, scale = 4)
    private BigDecimal endReading;

    @Column(name = "quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal unitPrice;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 0)
    private BigDecimal totalAmount;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "calculation_snapshot", nullable = false, columnDefinition = "jsonb")
    private JsonNode calculationSnapshot;

    @JdbcTypeCode(SqlTypes.JSON)
    @Builder.Default
    @Column(name = "evidence_refs", nullable = false, columnDefinition = "jsonb")
    private JsonNode evidenceRefs = JsonNodeFactory.instance.arrayNode();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RoomServiceChargeStatus status;

    @Column(name = "finalized_by", nullable = true)
    private UUID finalizedBy;

    @Column(name = "finalized_at", nullable = true, columnDefinition = "timestamptz")
    private Instant finalizedAt;
}
