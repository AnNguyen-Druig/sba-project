package com.sba.project.modules.notification.event;

import com.sba.project.modules.notification.dto.request.SendNotificationRequest;
import com.sba.project.modules.notification.enums.NotificationChannel;
import com.sba.project.modules.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Event listener for handling NotificationEvent published by domain modules.
 * Executes AFTER_COMMIT to ensure that database changes are committed before dispatching notifications.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final NotificationService notificationService;

    /**
     * Handle incoming notification events.
     * Fallback execution is enabled in case events are published outside an active transaction.
     *
     * @param event the published notification event
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleNotificationEvent(NotificationEvent event) {
        log.info("Received NotificationEvent for user={}, type={}, refType={}, refId={}",
                event.recipientUserId(), event.type(), event.referenceType(), event.referenceId());

        SendNotificationRequest request = SendNotificationRequest.builder()
                .recipientUserId(event.recipientUserId())
                .type(event.type())
                .title(event.title())
                .message(event.message())
                .referenceType(event.referenceType())
                .referenceId(event.referenceId())
                .channel(NotificationChannel.IN_APP)
                .build();

        notificationService.sendNotification(request);
    }
}
