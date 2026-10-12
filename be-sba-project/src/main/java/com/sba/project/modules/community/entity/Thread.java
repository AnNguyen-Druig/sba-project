package com.sba.project.modules.community.entity;

import com.sba.project.common.base.BaseEntity;
import com.sba.project.modules.community.enums.ThreadStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import tools.jackson.databind.JsonNode;

@Entity
@Table(name = "threads", schema = "rental")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Thread extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "author_user_id", nullable = false)
    private UUID authorUserId;

    @Column(name = "title", nullable = false, columnDefinition = "text")
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "text")
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "criteria", nullable = false, columnDefinition = "jsonb")
    private JsonNode criteria;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ThreadStatus status;

    @Column(name = "expires_at", nullable = true, columnDefinition = "timestamptz")
    private Instant expiresAt;

    @Column(name = "closed_at", nullable = true, columnDefinition = "timestamptz")
    private Instant closedAt;

    @Column(name = "moderation_reason", nullable = true, columnDefinition = "text")
    private String moderationReason;

    @Column(name = "moderated_by", nullable = true)
    private UUID moderatedBy;

    @Column(name = "moderated_at", nullable = true, columnDefinition = "timestamptz")
    private Instant moderatedAt;
}
