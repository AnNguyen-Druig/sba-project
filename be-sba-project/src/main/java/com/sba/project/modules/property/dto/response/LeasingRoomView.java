package com.sba.project.modules.property.dto.response;

import com.sba.project.modules.property.enums.BranchStatus;
import com.sba.project.modules.property.enums.RentalMode;
import com.sba.project.modules.property.enums.RoomOperationalStatus;
import java.math.BigDecimal;
import java.util.UUID;

/** Read-only Room + Branch facts the leasing module needs; no private Branch data (QR/Zalo). */
public record LeasingRoomView(
        UUID roomId,
        String roomCode,
        RentalMode rentalMode,
        Integer capacity,
        BigDecimal referencePrice,
        RoomOperationalStatus operationalStatus,
        UUID branchId,
        String branchName,
        BranchStatus branchStatus,
        UUID ownerId,
        UUID managerId) {
}
