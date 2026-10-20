package com.sba.project.modules.leasing.entity;

import com.sba.project.modules.leasing.enums.ContractAmendmentStatus;
import com.sba.project.modules.leasing.enums.ContractDocumentStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
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
@Table(name = "contract_amendments", schema = "rental")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ContractAmendment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "contract_id", nullable = false)
    private UUID contractId;

    @Column(name = "contract_tenant_id", nullable = true)
    private UUID contractTenantId;

    @Column(name = "amendment_number", nullable = false)
    private Integer amendmentNumber;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "previous_terms", nullable = false, columnDefinition = "jsonb")
    private JsonNode previousTerms;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "new_terms", nullable = false, columnDefinition = "jsonb")
    private JsonNode newTerms;

    @Column(name = "reason", nullable = false, columnDefinition = "text")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ContractAmendmentStatus status;

    @Column(name = "approved_by", nullable = true)
    private UUID approvedBy;

    @Column(name = "approved_at", nullable = true, columnDefinition = "timestamptz")
    private Instant approvedAt;

    @Column(name = "created_at", nullable = false, columnDefinition = "timestamptz", updatable = false)
    private Instant createdAt;

    @Column(name = "document_public_id", nullable = true, columnDefinition = "text")
    private String documentPublicId;

    @Column(name = "document_resource_type", nullable = true, length = 20)
    private String documentResourceType;

    @Column(name = "document_sha256", nullable = true, length = 64)
    private String documentSha256;

    @Column(name = "document_version", nullable = true)
    private Integer documentVersion;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "document_snapshot", nullable = true, columnDefinition = "jsonb")
    private JsonNode documentSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_status", nullable = true, length = 24)
    private ContractDocumentStatus documentStatus;

    @JdbcTypeCode(SqlTypes.JSON)
    @Builder.Default
    @Column(name = "confirmation_evidence", nullable = false, columnDefinition = "jsonb")
    private JsonNode confirmationEvidence = JsonNodeFactory.instance.arrayNode();

    @PrePersist
    protected void initializeInsertTimestamps() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
    }

    // Navigation only; assign contractId to change the foreign key.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contract_id", referencedColumnName = "id", insertable = false, updatable = false)
    @Setter(lombok.AccessLevel.NONE)
    private Contract contractReference;
}
