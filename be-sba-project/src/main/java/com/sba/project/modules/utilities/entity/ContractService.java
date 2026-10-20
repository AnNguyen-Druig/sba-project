package com.sba.project.modules.utilities.entity;

import com.sba.project.modules.utilities.enums.ContractServiceStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "contract_services", schema = "rental")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ContractService {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "contract_id", nullable = false)
    private UUID contractId;

    @Column(name = "contract_tenant_id", nullable = true)
    private UUID contractTenantId;

    @Column(name = "room_id", nullable = false)
    private UUID roomId;

    @Column(name = "room_service_id", nullable = false)
    private UUID roomServiceId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "agreed_unit_price", nullable = true, precision = 19, scale = 4)
    private BigDecimal agreedUnitPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 24)
    private ContractServiceStatus status;

    @Column(name = "requested_by", nullable = false)
    private UUID requestedBy;

    @Column(name = "processed_by", nullable = true)
    private UUID processedBy;

    @Column(name = "processed_at", nullable = true, columnDefinition = "timestamptz")
    private Instant processedAt;

    @Column(name = "reason", nullable = true, columnDefinition = "text")
    private String reason;
}
