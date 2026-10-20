package com.sba.project.modules.utilities.entity;

import com.sba.project.modules.utilities.enums.BillingMethod;
import com.sba.project.modules.utilities.enums.ServiceType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "services", schema = "rental")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ServiceCatalog {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "name", nullable = false, columnDefinition = "text")
    private String name;

    @Column(name = "unit", nullable = false, length = 30)
    private String unit;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false, length = 20)
    private ServiceType serviceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_method", nullable = false, length = 20)
    private BillingMethod billingMethod;

    @Column(name = "reference_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal referencePrice;

    @Column(name = "mandatory", nullable = false)
    private Boolean mandatory;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private Boolean active = true;
}
