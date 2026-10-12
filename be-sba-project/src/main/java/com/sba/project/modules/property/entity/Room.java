package com.sba.project.modules.property.entity;

import com.sba.project.common.base.BaseEntity;
import com.sba.project.modules.property.enums.RentalMode;
import com.sba.project.modules.property.enums.RoomOperationalStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;

@Entity
@Table(name = "rooms", schema = "rental")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Room extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    @Column(name = "room_type_id", nullable = false)
    private UUID roomTypeId;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "rental_mode", nullable = false, length = 4)
    private RentalMode rentalMode;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Column(name = "area_sqm", nullable = true, precision = 10, scale = 2)
    private BigDecimal areaSqm;

    @Column(name = "floor_number", nullable = true)
    private Integer floorNumber;

    @Column(name = "reference_price", nullable = false, precision = 19, scale = 0)
    private BigDecimal referencePrice;

    @Column(name = "description", nullable = true, columnDefinition = "text")
    private String description;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Builder.Default
    @Column(name = "amenities", nullable = false, columnDefinition = "text[]")
    private String[] amenities = new String[0];

    @JdbcTypeCode(SqlTypes.JSON)
    @Builder.Default
    @Column(name = "image_refs", nullable = false, columnDefinition = "jsonb")
    private JsonNode imageRefs = JsonNodeFactory.instance.arrayNode();

    @Enumerated(EnumType.STRING)
    @Column(name = "operational_status", nullable = false, length = 20)
    private RoomOperationalStatus operationalStatus;

    @Builder.Default
    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = false;

    // Guardian counter maintained by database triggers; refresh after related writes.
    @Column(name = "version", nullable = false, insertable = false, updatable = false)
    private Long version;

    // Navigation only; assign branchId to change the foreign key.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", referencedColumnName = "id", insertable = false, updatable = false)
    @Setter(lombok.AccessLevel.NONE)
    private Branch branchReference;
}
