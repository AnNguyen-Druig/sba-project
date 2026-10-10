package com.sba.project.entity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
@Entity
@Table(name = "room_services", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"roomId", "serviceId"}, name = "uk_room_service")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class RoomService extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID roomId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "serviceId", nullable = false)
    private Service service;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice; 

    @Column(nullable = false)
    private LocalDate effectiveFrom; 

    private LocalDate effectiveTo; 

    @Column(nullable = false)
    private boolean active;
}
