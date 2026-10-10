package com.sba.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "appointment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "appointment_id", updatable = false, nullable = false)
    private UUID appointmentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "appointment_at", nullable = false)
    private LocalDateTime appointmentAt;

    @Column(name = "status", nullable = false, length = 255)
    private String status;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;
}
