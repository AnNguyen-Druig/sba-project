package com.sba.project.modules.notification.repository;

import com.sba.project.modules.notification.entity.Notification;
import com.sba.project.modules.notification.enums.NotificationChannel;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Data access repository for Notification entities.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID>, JpaSpecificationExecutor<Notification> {

    /**
     * Retrieve all notifications for a recipient, sorted newest first.
     */
    Page<Notification> findByRecipientUserIdOrderByCreatedAtDesc(UUID recipientUserId, Pageable pageable);

    /**
     * Retrieve unread notifications for a recipient, sorted newest first.
     */
    Page<Notification> findByRecipientUserIdAndReadAtIsNullOrderByCreatedAtDesc(UUID recipientUserId, Pageable pageable);

    /**
     * Retrieve read notifications for a recipient, sorted newest first.
     */
    Page<Notification> findByRecipientUserIdAndReadAtIsNotNullOrderByCreatedAtDesc(UUID recipientUserId, Pageable pageable);

    /**
     * Count unread notifications for a recipient.
     */
    long countByRecipientUserIdAndReadAtIsNull(UUID recipientUserId);

    /**
     * Find specific notifications belonging to a recipient.
     */
    List<Notification> findByIdInAndRecipientUserId(List<UUID> ids, UUID recipientUserId);

    /**
     * Find a single notification belonging to a recipient.
     */
    Optional<Notification> findByIdAndRecipientUserId(UUID id, UUID recipientUserId);

    /**
     * Check if a duplicate notification already exists.
     */
    boolean existsByRecipientUserIdAndEventKeyAndTypeAndChannel(
            UUID recipientUserId, String eventKey, String type, NotificationChannel channel);

    /**
     * Find existing notification matching deduplication criteria.
     */
    Optional<Notification> findFirstByRecipientUserIdAndEventKeyAndTypeAndChannel(
            UUID recipientUserId, String eventKey, String type, NotificationChannel channel);

    /**
     * Bulk mark all unread notifications as read for a recipient.
     */
    @Modifying
    @Query("UPDATE Notification n SET n.readAt = :readAt WHERE n.recipientUserId = :recipientUserId AND n.readAt IS NULL")
    int markAllAsReadByRecipientUserId(@Param("recipientUserId") UUID recipientUserId, @Param("readAt") Instant readAt);
}
