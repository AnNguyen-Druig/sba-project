package com.sba.project.modules.property.service;

import com.sba.project.modules.property.dto.response.BranchPolicyView;
import com.sba.project.modules.property.dto.response.LeasingRoomView;
import com.sba.project.modules.property.dto.response.LeasingSlotView;
import java.util.UUID;

/**
 * Read API that the property module publishes for other modules (RULE 10). Callers never touch
 * property repositories or entities directly.
 */
public interface PropertyQueryService {
    /** Throws {@code ROOM_NOT_FOUND}. */
    LeasingRoomView getRoom(UUID roomId);

    /** Throws {@code ROOM_SLOT_NOT_FOUND}. */
    LeasingSlotView getSlot(UUID slotId);

    /** Policy in force now; throws {@code BRANCH_POLICY_NOT_FOUND} when the Branch has none (no hardcoded default). */
    BranchPolicyView currentPolicy(UUID branchId);
}
