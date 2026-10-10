package com.sba.project.entity;
import com.sba.project.enums.BillingMethod;
import com.sba.project.enums.ServiceStatus;
import com.sba.project.enums.ServiceType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;
@Entity
@Table(name = "services")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Service extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceType serviceType;

    @Column(nullable = false)
    private String unit; 

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BillingMethod billingMethod;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal defaultUnitPrice; 

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceStatus status;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private boolean isDefault; 

    @Column(nullable = false)
    private UUID branchId;

    @Column(nullable = false)
    private UUID createdBy;
}
