package com.sba.project.modules.property.dto.response;

import com.sba.project.modules.property.enums.RentalMode;
import com.sba.project.modules.property.enums.RoomOperationalStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import tools.jackson.databind.JsonNode;

@Value @Builder
public class RoomResponse {
    UUID id; UUID branchId; UUID roomTypeId; String code; RentalMode rentalMode; Integer capacity;
    BigDecimal areaSqm; Integer floorNumber; BigDecimal referencePrice; String description;
    String[] amenities; JsonNode imageRefs; RoomOperationalStatus operationalStatus; Boolean isPublic;
    Instant createdAt; Instant updatedAt; Long version;
}
