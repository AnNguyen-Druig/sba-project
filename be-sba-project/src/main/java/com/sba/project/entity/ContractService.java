package com.sba.project.entity;
import com.sba.project.enums.ServiceRegistrationStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
@Entity
@Table(name = "contract_services")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ContractService extends BaseEntity {

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

    private UUID requestedBy;

    private UUID processedBy;

    private LocalDateTime processedAt;

    private String rejectionReason;
}
