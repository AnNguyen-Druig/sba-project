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

/**
 * Danh mục dịch vụ (điện, nước, internet, etc.)
 * [P1-68] Owner/Manager quản lý danh mục Services
 */
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
    private String unit; // đơn vị: kWh, m3, người, phòng, lượt

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BillingMethod billingMethod;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal defaultUnitPrice; // Đơn giá tham chiếu

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceStatus status;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private boolean isDefault; // true for ELECTRICITY, WATER

    // Branch scope - service belongs to a branch (managed by Owner/Manager)
    @Column(nullable = false)
    private UUID branchId;

    // Who created this service catalog entry
    @Column(nullable = false)
    private UUID createdBy;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
