package com.sba.project.entity;
import com.sba.project.enums.BillingMethod;
import com.sba.project.enums.ServiceStatus;
import com.sba.project.enums.ServiceType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
@Entity
@Table(name = "services")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Service {

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

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
