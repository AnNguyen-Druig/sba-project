package com.sba.project.modules.maintenance.entity;

import com.sba.project.modules.maintenance.enums.FurnitureStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "furnitures", schema = "rental")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Furniture {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "room_id", nullable = false)
    private UUID roomId;

    @Column(name = "asset_code", nullable = false, length = 50)
    private String assetCode;

    @Column(name = "name", nullable = false, columnDefinition = "text")
    private String name;

    @Column(name = "furniture_type", nullable = true, length = 100)
    private String furnitureType;

    @Column(name = "serial_number", nullable = true, columnDefinition = "text")
    private String serialNumber;

    @Column(name = "purchase_date", nullable = true)
    private LocalDate purchaseDate;

    @Column(name = "asset_value", nullable = true, precision = 19, scale = 0)
    private BigDecimal assetValue;

    @Column(name = "condition", nullable = true, columnDefinition = "text")
    private String condition;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private FurnitureStatus status;
}
