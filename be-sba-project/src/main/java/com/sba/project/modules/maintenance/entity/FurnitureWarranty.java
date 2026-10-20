package com.sba.project.modules.maintenance.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "furniture_warranties", schema = "rental")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class FurnitureWarranty {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "furniture_id", nullable = false)
    private UUID furnitureId;

    @Column(name = "provider", nullable = false, columnDefinition = "text")
    private String provider;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "terms", nullable = true, columnDefinition = "text")
    private String terms;

    @Column(name = "warranty_code", nullable = true, columnDefinition = "text")
    private String warrantyCode;

    // Navigation only; assign furnitureId to change the foreign key.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "furniture_id", referencedColumnName = "id", insertable = false, updatable = false)
    @Setter(lombok.AccessLevel.NONE)
    private Furniture furnitureReference;
}
