package com.sba.project.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@SQLRestriction("is_deleted = false")
@Table(name = "room_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomType extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "room_type_id", updatable = false, nullable = false)
    private UUID roomTypeId;

    @Column(name = "type_name", nullable = false, length = 255)
    private String typeName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "default_capacity", nullable = false)
    private Integer defaultCapacity;

    @Column(name = "features", columnDefinition = "TEXT")
    private String features;

    @Column(name = "is_deleted", nullable = false, columnDefinition = "boolean default false")
    private boolean deleted;
}
