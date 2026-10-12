package com.sba.project.modules.identity.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sba.project.common.base.BaseEntity;
import com.sba.project.modules.identity.enums.OwnerVerificationStatus;
import com.sba.project.modules.identity.enums.UserStatus;
import com.sba.project.modules.identity.enums.VerificationPurpose;
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
@Table(name = "users", schema = "rental")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "email", nullable = true, length = 320)
    private String email;

    @Column(name = "phone", nullable = true, length = 32)
    private String phone;

    @JsonIgnore
    @Column(name = "password_hash", nullable = true, columnDefinition = "text")
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 24)
    private UserStatus status;

    @Column(name = "date_of_birth", nullable = true)
    private LocalDate dateOfBirth;

    @Column(name = "gender", nullable = true, length = 30)
    private String gender;

    @Column(name = "identity_document_public_id", nullable = true, columnDefinition = "text")
    private String identityDocumentPublicId;

    @Column(name = "identity_document_resource_type", nullable = true, length = 20)
    private String identityDocumentResourceType;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "owner_verification_status", nullable = false, length = 20)
    private OwnerVerificationStatus ownerVerificationStatus = OwnerVerificationStatus.NOT_SUBMITTED;

    @Column(name = "owner_document_public_id", nullable = true, columnDefinition = "text")
    private String ownerDocumentPublicId;

    @Column(name = "owner_document_resource_type", nullable = true, length = 20)
    private String ownerDocumentResourceType;

    @Column(name = "owner_reviewed_by", nullable = true)
    private UUID ownerReviewedBy;

    @Column(name = "owner_reviewed_at", nullable = true, columnDefinition = "timestamptz")
    private Instant ownerReviewedAt;

    @Column(name = "owner_review_reason", nullable = true, columnDefinition = "text")
    private String ownerReviewReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_purpose", nullable = true, length = 24)
    private VerificationPurpose verificationPurpose;

    @JsonIgnore
    @Column(name = "verification_secret_hash", nullable = true, columnDefinition = "text")
    private String verificationSecretHash;

    @Column(name = "verification_expires_at", nullable = true, columnDefinition = "timestamptz")
    private Instant verificationExpiresAt;

    @Builder.Default
    @Column(name = "verification_attempts", nullable = false)
    private Integer verificationAttempts = 0;

    @Column(name = "verification_max_attempts", nullable = true)
    private Integer verificationMaxAttempts;

    @Column(name = "verification_consumed_at", nullable = true, columnDefinition = "timestamptz")
    private Instant verificationConsumedAt;

    @Builder.Default
    @Column(name = "failed_login_count", nullable = false)
    private Integer failedLoginCount = 0;

    @Column(name = "last_failed_login_at", nullable = true, columnDefinition = "timestamptz")
    private Instant lastFailedLoginAt;

    @JsonIgnore
    @Column(name = "refresh_token_hash", nullable = true, columnDefinition = "text")
    private String refreshTokenHash;

    @Column(name = "refresh_token_expires_at", nullable = true, columnDefinition = "timestamptz")
    private Instant refreshTokenExpiresAt;

    @Column(name = "email_verified_at", nullable = true, columnDefinition = "timestamptz")
    private Instant emailVerifiedAt;

    @Column(name = "phone_verified_at", nullable = true, columnDefinition = "timestamptz")
    private Instant phoneVerifiedAt;

    @Column(name = "locked_until", nullable = true, columnDefinition = "timestamptz")
    private Instant lockedUntil;

    @Builder.Default
    @Column(name = "auth_version", nullable = false)
    private Long authVersion = 0L;

    @JdbcTypeCode(SqlTypes.JSON)
    @Builder.Default
    @Column(name = "notification_preferences", nullable = false, columnDefinition = "jsonb")
    private JsonNode notificationPreferences = JsonNodeFactory.instance.objectNode();

    // Navigation only; assign ownerReviewedBy to change the foreign key.
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "owner_reviewed_by", referencedColumnName = "id", insertable = false, updatable = false)
    @Setter(lombok.AccessLevel.NONE)
    private User ownerReviewedByReference;
}
