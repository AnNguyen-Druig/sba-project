package com.sba.project.modules.notification.mapper;

import com.sba.project.modules.notification.dto.request.SendNotificationRequest;
import com.sba.project.modules.notification.dto.response.NotificationResponse;
import com.sba.project.modules.notification.entity.Notification;
import com.sba.project.modules.notification.enums.NotificationChannel;
import java.util.List;
import java.util.UUID;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * MapStruct mapper for converting between Notification entities and DTOs.
 * Implements RULE 01.
 */
@Mapper(componentModel = "spring")
public interface NotificationMapper {

    /**
     * Convert Notification entity to NotificationResponse DTO.
     * Computes the read flag based on whether readAt is present.
     */
    @Mapping(target = "read", expression = "java(notification.getReadAt() != null)")
    NotificationResponse toResponse(Notification notification);

    /**
     * Convert a list of Notification entities to a list of NotificationResponse DTOs.
     */
    List<NotificationResponse> toResponseList(List<Notification> notifications);

    /**
     * Map SendNotificationRequest to Notification entity.
     * Automatically computes eventKey for deduplication.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "readAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "attemptCount", ignore = true)
    @Mapping(target = "nextAttemptAt", ignore = true)
    @Mapping(target = "sentAt", ignore = true)
    @Mapping(target = "providerMessageId", ignore = true)
    @Mapping(target = "lastError", ignore = true)
    @Mapping(target = "deliveryStatus", ignore = true)
    @Mapping(target = "eventKey", expression = "java(generateEventKey(request))")
    Notification toEntity(SendNotificationRequest request);

    /**
     * Generate deduplication eventKey following the convention:
     * - type + ":" + referenceType + ":" + referenceId
     * - or type + ":" + UUID if no referenceType
     */
    default String generateEventKey(SendNotificationRequest request) {
        if (request == null) {
            return null;
        }
        String type = request.getType();
        String refType = request.getReferenceType();
        UUID refId = request.getReferenceId();

        if (refType != null && !refType.isBlank()) {
            if (refId != null) {
                return type + ":" + refType + ":" + refId;
            }
            return type + ":" + refType + ":" + UUID.randomUUID();
        }
        return type + ":" + UUID.randomUUID();
    }

    @AfterMapping
    default void setDefaultValues(@MappingTarget Notification notification, SendNotificationRequest request) {
        if (notification.getChannel() == null) {
            notification.setChannel(NotificationChannel.IN_APP);
        }
    }
}
