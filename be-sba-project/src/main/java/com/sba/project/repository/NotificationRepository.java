package com.sba.project.repository;
import com.sba.project.entity.Notification;
import com.sba.project.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.UUID;
@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    Page<Notification> findByRecipientIdOrderByCreatedAtDesc(UUID recipientId, Pageable pageable);
    Page<Notification> findByRecipientIdAndIsReadOrderByCreatedAtDesc(
        UUID recipientId, boolean isRead, Pageable pageable
    );
    Page<Notification> findByRecipientIdAndTypeOrderByCreatedAtDesc(
        UUID recipientId, NotificationType type, Pageable pageable
    );
    long countByRecipientIdAndIsReadFalse(UUID recipientId);
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.recipientId = :recipientId AND n.isRead = false")
    int markAllAsRead(@Param("recipientId") UUID recipientId);
}
