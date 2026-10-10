package com.sba.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "room_slots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomSlot extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "room_slot_id", updatable = false, nullable = false)
    private UUID roomSlotId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "slot_code", nullable = false, length = 255)
    private String slotCode;

    @Column(name = "slot_name", nullable = false, length = 255)
    private String slotName;

    @Column(name = "status", nullable = false, length = 255)
    private String status;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}
