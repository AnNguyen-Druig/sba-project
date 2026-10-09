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
 * Phí dịch vụ theo kỳ - kết quả tính phí cho từng phòng
 * [P1-74] Nhập chỉ số điện/nước đầu kỳ, cuối kỳ
 * [P1-75] Tính lượng dùng, đơn giá, tổng tiền
 * [P1-76] Tính dịch vụ kiểu theo chỉ số, theo người, theo phòng...
 * [P1-78] Cho phép xem lại nguồn gốc số tiền dịch vụ
 */
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

    // Kỳ tính phí
    @Column(nullable = false)
    private LocalDate billingPeriodStart;

    @Column(nullable = false)
    private LocalDate billingPeriodEnd;

    // Chỉ số (cho điện/nước)
    private BigDecimal previousReading; // Chỉ số đầu kỳ
    private BigDecimal currentReading;  // Chỉ số cuối kỳ

    // Kết quả tính phí
    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal quantity; // Lượng dùng (kWh, m3, số người, 1 nếu cố định)

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice; // Đơn giá tại thời điểm tính

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount; // = quantity * unitPrice

    @Column(nullable = false)
    private boolean finalized; // Đã chốt - không cho sửa

    // Người nhập chỉ số / tạo charge
    @Column(nullable = false)
    private UUID createdBy;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
