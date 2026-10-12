package com.sba.project.modules.maintenance.entity;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "report_requests_furnitures", schema = "rental")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ReportRequestFurniture {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "report_request_id", nullable = false)
    private UUID reportRequestId;

    @Column(name = "room_id", nullable = false)
    private UUID roomId;

    @Column(name = "furniture_id", nullable = false)
    private UUID furnitureId;

    @Column(name = "damage_description", nullable = true, columnDefinition = "text")
    private String damageDescription;
}
