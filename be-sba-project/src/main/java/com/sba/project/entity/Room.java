package com.sba.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "rooms", uniqueConstraints = @UniqueConstraint(columnNames = {"branch_id", "room_code"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "room_id", updatable = false, nullable = false)
    private UUID roomId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "manager_id", nullable = false)
    private Manager manager;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_type_id", nullable = false)
    private RoomType roomType;

    @Column(name = "room_code", nullable = false, length = 255)
    private String roomCode;

    @Column(name = "reference_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal referencePrice;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Column(name = "status", nullable = false, length = 255)
    private String status;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}
