package com.sba.project.modules.utilities.repository;

import com.sba.project.modules.utilities.entity.CostAllocationProposal;
import com.sba.project.modules.utilities.enums.CostAllocationProposalStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for {@link CostAllocationProposal} entity.
 */
@Repository
public interface CostAllocationProposalRepository extends JpaRepository<CostAllocationProposal, UUID>, JpaSpecificationExecutor<CostAllocationProposal> {

    List<CostAllocationProposal> findByRoomServiceChargeIdAndStatus(UUID chargeId, CostAllocationProposalStatus status);

    Optional<CostAllocationProposal> findByRoomServiceChargeIdAndStatusIn(UUID chargeId, List<CostAllocationProposalStatus> statuses);

    List<CostAllocationProposal> findByRoomServiceChargeId(UUID chargeId);
}
