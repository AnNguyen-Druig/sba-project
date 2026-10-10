package com.sba.project.entity;
import com.sba.project.enums.AllocationStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
@Entity
@Table(name = "cost_allocation_proposals")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CostAllocationProposal extends BaseEntity {

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
    private String reason; 

    @Column(nullable = false)
    private UUID proposedBy; 

    private UUID processedBy; 

    private LocalDateTime processedAt;

    private String rejectionReason;

    @OneToMany(mappedBy = "proposal", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CostAllocationItem> items = new ArrayList<>();
}
