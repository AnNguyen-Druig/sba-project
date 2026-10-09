package com.sba.project.entity;

import com.sba.project.enums.ServiceRegistrationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Dịch vụ gắn với hợp đồng - bao gồm cả dịch vụ mặc định và tùy chọn
 * [P1-71] Tenant đăng ký/hủy dịch vụ tùy chọn
 * [P1-72] Owner/Manager duyệt đăng ký/hủy dịch vụ
 * [P1-70] Điện, nước mặc định không cho Tenant tự hủy
 */
@Entity
@Table(name = "contract_services")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ContractService {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID contractId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roomServiceId", nullable = false)
    private RoomService roomService;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceRegistrationStatus registrationStatus;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    // Tenant who requested registration/cancellation
    private UUID requestedBy;

    // Manager/Owner who approved/rejected
    private UUID processedBy;

    private LocalDateTime processedAt;

    private String rejectionReason;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
