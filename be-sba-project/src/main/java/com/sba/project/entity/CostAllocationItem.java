package com.sba.project.entity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;
@Entity
@Table(name = "cost_allocation_items")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CostAllocationItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposalId", nullable = false)
    private CostAllocationProposal proposal;

    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount; 

    @Column(precision = 5, scale = 2)
    private BigDecimal percentage; 

    private String note;
}
