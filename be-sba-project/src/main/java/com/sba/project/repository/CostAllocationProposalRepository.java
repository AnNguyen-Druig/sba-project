package com.sba.project.repository;
import com.sba.project.entity.CostAllocationProposal;
import com.sba.project.enums.AllocationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface CostAllocationProposalRepository extends JpaRepository<CostAllocationProposal, UUID> {
    Page<CostAllocationProposal> findByRoomId(UUID roomId, Pageable pageable);
    List<CostAllocationProposal> findByRoomServiceChargeId(UUID roomServiceChargeId);
    Optional<CostAllocationProposal> findByRoomServiceChargeIdAndStatus(
        UUID roomServiceChargeId, AllocationStatus status
    );
    List<CostAllocationProposal> findByProposedBy(UUID tenantId);
    Page<CostAllocationProposal> findByRoomIdAndStatus(UUID roomId, AllocationStatus status, Pageable pageable);
}
