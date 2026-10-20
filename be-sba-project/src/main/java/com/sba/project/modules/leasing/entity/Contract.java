package com.sba.project.modules.leasing.entity;

import com.sba.project.common.base.BaseEntity;
import com.sba.project.modules.leasing.enums.ContractDocumentStatus;
import com.sba.project.modules.leasing.enums.ContractStatus;
import com.sba.project.modules.leasing.enums.PaymentEnvironment;
import com.sba.project.modules.leasing.enums.RentalMode;
import jakarta.persistence.*;
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
@Table(name = "contracts", schema = "rental")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Contract extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    @Column(name = "room_id", nullable = false)
    private UUID roomId;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "rental_mode", nullable = false, length = 4)
    private RentalMode rentalMode;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_environment", nullable = false, length = 10)
    private PaymentEnvironment paymentEnvironment;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "terms", nullable = false, columnDefinition = "text")
    private String terms;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ContractStatus status;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    // Scalar revision; no automatic JPA optimistic locking.
    @Builder.Default
    @Column(name = "version", nullable = false)
    private Long version = 0L;

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
}
