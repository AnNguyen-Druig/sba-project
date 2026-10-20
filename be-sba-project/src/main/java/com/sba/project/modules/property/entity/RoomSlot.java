package com.sba.project.modules.property.entity;

import com.sba.project.modules.property.enums.RoomSlotStatus;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "room_slots", schema = "rental")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class RoomSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "room_id", nullable = false)
    private UUID roomId;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "name", nullable = false, columnDefinition = "text")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RoomSlotStatus status;

    // Navigation only; assign roomId to change the foreign key.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", referencedColumnName = "id", insertable = false, updatable = false)
    @Setter(lombok.AccessLevel.NONE)
    private Room roomReference;
}
