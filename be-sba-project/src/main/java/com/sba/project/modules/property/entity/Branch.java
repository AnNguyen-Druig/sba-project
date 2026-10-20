package com.sba.project.modules.property.entity;

import com.sba.project.common.base.BaseEntity;
import com.sba.project.modules.property.enums.BranchStatus;
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
@Table(name = "branches", schema = "rental")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Branch extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "manager_id", nullable = true)
    private UUID managerId;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "address", nullable = false, columnDefinition = "text")
    private String address;

    @Column(name = "province", nullable = true, length = 100)
    private String province;

    @Column(name = "district", nullable = true, length = 100)
    private String district;

    @Column(name = "ward", nullable = true, length = 100)
    private String ward;

    @Column(name = "latitude", nullable = true, precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "longitude", nullable = true, precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "description", nullable = true, columnDefinition = "text")
    private String description;

    @Column(name = "rules", nullable = true, columnDefinition = "text")
    private String rules;

    @Column(name = "public_contact", nullable = true, columnDefinition = "text")
    private String publicContact;

    @Builder.Default
    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = false;

    @JdbcTypeCode(SqlTypes.JSON)
    @Builder.Default
    @Column(name = "image_refs", nullable = false, columnDefinition = "jsonb")
    private JsonNode imageRefs = JsonNodeFactory.instance.arrayNode();

    @Column(name = "qr_public_id", nullable = true, columnDefinition = "text")
    private String qrPublicId;

    @Column(name = "qr_url", nullable = true, columnDefinition = "text")
    private String qrUrl;

    @Column(name = "zalo_group_url", nullable = true, columnDefinition = "text")
    private String zaloGroupUrl;

    @Builder.Default
    @Column(name = "zalo_enabled", nullable = false)
    private Boolean zaloEnabled = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BranchStatus status;

    // Guardian counter maintained by database triggers; refresh after related writes.
    @Column(name = "version", nullable = false, insertable = false, updatable = false)
    private Long version;
}
