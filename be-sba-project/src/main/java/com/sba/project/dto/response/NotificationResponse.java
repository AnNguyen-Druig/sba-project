package com.sba.project.dto.response;
import com.sba.project.enums.NotificationType;
import lombok.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private UUID id;

    private NotificationType type;

    private String title;

    private String message;

    private boolean isRead;

    private UUID referenceId;

    private String referenceType;

    private UUID branchId;

    private Instant createdAt;

    private LocalDateTime readAt;
}
