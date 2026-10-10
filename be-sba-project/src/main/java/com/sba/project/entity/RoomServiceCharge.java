package com.sba.project.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
@Entity
@Table(name = "room_service_charges", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"roomServiceId", "billingPeriodStart", "billingPeriodEnd"}, name = "uk_room_service_charge_period")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class RoomServiceCharge {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roomServiceId", nullable = false)
    private RoomService roomService;

    @Column(nullable = false)
    private UUID roomId;

    @Column(nullable = false)
    private LocalDate billingPeriodStart;

    @Column(nullable = false)
    private LocalDate billingPeriodEnd;

    private BigDecimal previousReading; 

    private BigDecimal currentReading;  

    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal quantity; 

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice; 

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount; 

    @Column(nullable = false)
    private boolean finalized; 

    @Column(nullable = false)
    private UUID createdBy;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
