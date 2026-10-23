package com.sba.project.modules.property.dto.response;

import com.sba.project.modules.property.enums.RoomSlotStatus;
import java.util.UUID;

public record LeasingSlotView(UUID slotId, UUID roomId, String code, String name, RoomSlotStatus status) {
}
