package com.sba.project.entity;

import com.sba.project.enums.AllocationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Đề xuất phân bổ chi phí điện/nước giữa các Tenant ở ghép
 * [P1-80] Tenant gửi yêu cầu điều chỉnh tỷ lệ/số tiền
 * [P1-81] Manager/Owner duyệt
 * [P1-82] Tổng phân bổ phải khớp phí gốc
 * [P1-83] Chỉ phương án duyệt mới dùng chốt khoản phải trả
 */
@Entity
@Table(name = "cost_allocation_proposals")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CostAllocationProposal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roomServiceChargeId", nullable = false)
    private RoomServiceCharge roomServiceCharge;

    @Column(nullable = false)
    private UUID roomId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AllocationStatus status;

    @Column(columnDefinition = "TEXT")
    private String reason; // Lý do đề xuất

    @Column(nullable = false)
    private UUID proposedBy; // Tenant đề xuất

    private UUID processedBy; // Manager/Owner duyệt
    private LocalDateTime processedAt;
    private String rejectionReason;

    @OneToMany(mappedBy = "proposal", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CostAllocationItem> items = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
