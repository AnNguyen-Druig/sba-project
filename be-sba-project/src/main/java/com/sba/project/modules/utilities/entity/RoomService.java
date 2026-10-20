package com.sba.project.modules.utilities.entity;

import com.sba.project.modules.utilities.enums.RoomServiceStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
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
@Table(name = "room_services", schema = "rental")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class RoomService {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "room_id", nullable = false)
    private UUID roomId;

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    @Column(name = "service_id", nullable = false)
    private UUID serviceId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal unitPrice;

    @JdbcTypeCode(SqlTypes.JSON)
    @Builder.Default
    @Column(name = "billing_config", nullable = false, columnDefinition = "jsonb")
    private JsonNode billingConfig = JsonNodeFactory.instance.objectNode();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RoomServiceStatus status;
}
