package com.sba.project.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Dịch vụ áp dụng cho phòng cụ thể với đơn giá riêng
 * [P1-69] Cấu hình dịch vụ và đơn giá áp dụng theo phòng/Branch
 * [P1-73] Quản lý dịch vụ áp dụng cho phòng
 */
@Entity
@Table(name = "room_services", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"roomId", "serviceId"}, name = "uk_room_service")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class RoomService {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID roomId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "serviceId", nullable = false)
    private Service service;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice; // Đơn giá áp dụng cho phòng này (có thể khác defaultUnitPrice)

    @Column(nullable = false)
    private LocalDate effectiveFrom; // Ngày bắt đầu áp dụng

    private LocalDate effectiveTo; // Ngày kết thúc (null = vô thời hạn)

    @Column(nullable = false)
    private boolean active;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
