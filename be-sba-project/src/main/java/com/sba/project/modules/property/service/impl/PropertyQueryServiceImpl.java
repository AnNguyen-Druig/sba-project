package com.sba.project.modules.property.service.impl;

import com.sba.project.common.exceptions.AppException;
import com.sba.project.common.exceptions.ErrorCode;
import com.sba.project.modules.property.dto.response.BranchPolicyView;
import com.sba.project.modules.property.dto.response.LeasingRoomView;
import com.sba.project.modules.property.dto.response.LeasingSlotView;
import com.sba.project.modules.property.entity.Branch;
import com.sba.project.modules.property.entity.BranchPolicy;
import com.sba.project.modules.property.entity.Room;
import com.sba.project.modules.property.entity.RoomSlot;
import com.sba.project.modules.property.repository.BranchPolicyRepository;
import com.sba.project.modules.property.repository.BranchRepository;
import com.sba.project.modules.property.repository.RoomRepository;
import com.sba.project.modules.property.repository.RoomSlotRepository;
import com.sba.project.modules.property.service.PropertyQueryService;
import java.time.Clock;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PropertyQueryServiceImpl implements PropertyQueryService {
    private final RoomRepository roomRepository;
    private final RoomSlotRepository slotRepository;
    private final BranchPolicyRepository policyRepository;
    private final BranchRepository branchRepository;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public LeasingRoomView getRoom(UUID roomId) {
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));
        Branch branch = room.getBranchReference();
        return new LeasingRoomView(room.getId(), room.getCode(), room.getRentalMode(), room.getCapacity(),
                room.getReferencePrice(), room.getOperationalStatus(), branch.getId(), branch.getName(),
                branch.getStatus(), branch.getOwnerId(), branch.getManagerId());
    }

    @Override
    @Transactional(readOnly = true)
    public LeasingSlotView getSlot(UUID slotId) {
        RoomSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_SLOT_NOT_FOUND));
        return new LeasingSlotView(slot.getId(), slot.getRoomId(), slot.getCode(), slot.getName(), slot.getStatus());
    }

    @Override
    @Transactional(readOnly = true)
    public BranchPolicyView currentPolicy(UUID branchId) {
        return toView(policyRepository
                .findFirstByBranchIdAndEffectiveFromLessThanEqualOrderByVersionDesc(branchId, clock.instant())
                .orElseThrow(() -> new AppException(ErrorCode.BRANCH_POLICY_NOT_FOUND)));
    }

    @Override
    @Transactional(readOnly = true)
    public BranchPolicyView getPolicy(UUID policyId) {
        return toView(policyRepository.findById(policyId)
                .orElseThrow(() -> new AppException(ErrorCode.BRANCH_POLICY_NOT_FOUND)));
    }

    @Override
    @Transactional(readOnly = true)
    public String getPaymentQrUrl(UUID branchId) {
        return branchRepository.findById(branchId).map(Branch::getQrUrl)
                .orElseThrow(() -> new AppException(ErrorCode.BRANCH_NOT_FOUND));
    }

    private static BranchPolicyView toView(BranchPolicy policy) {
        return new BranchPolicyView(policy.getId(), policy.getBranchId(), policy.getVersion(),
                policy.getEffectiveFrom(), policy.getBookingHoldAmount(), policy.getPaymentWindowMinutes(),
                policy.getTemporaryHoldMinutes(), policy.getConfirmedHoldMinutes(), policy.getCancellationTerms());
    }
}
