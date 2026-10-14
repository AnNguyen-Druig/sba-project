package com.sba.project.modules.utilities.service.impl;

import com.sba.project.common.exceptions.AppException;
import com.sba.project.common.exceptions.ErrorCode;
import com.sba.project.modules.utilities.dto.request.CreateCostAllocationProposalRequest;
import com.sba.project.modules.utilities.dto.request.ProcessCostAllocationRequest;
import com.sba.project.modules.utilities.dto.response.CostAllocationProposalResponse;
import com.sba.project.modules.utilities.entity.ContractService;
import com.sba.project.modules.utilities.entity.CostAllocationProposal;
import com.sba.project.modules.utilities.entity.RoomServiceCharge;
import com.sba.project.modules.utilities.enums.AllocationMethod;
import com.sba.project.modules.utilities.enums.ContractServiceStatus;
import com.sba.project.modules.utilities.enums.CostAllocationProposalStatus;
import com.sba.project.modules.utilities.mapper.CostAllocationProposalMapper;
import com.sba.project.modules.utilities.repository.ContractServiceRepository;
import com.sba.project.modules.utilities.repository.CostAllocationProposalRepository;
import com.sba.project.modules.utilities.repository.RoomServiceChargeRepository;
import com.sba.project.modules.utilities.service.CostAllocationService;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

/**
 * Implementation of {@link CostAllocationService} covering BE-04.3.
 * Handles default split calculation by resident days using largest remainder rounding [P1-79],
 * tenant proposals [P1-80], manager approval [P1-81], exact charge matching [P1-82],
 * and proposal lifecycle without altering finalized invoices [P1-83][P1-84].
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CostAllocationServiceImpl implements CostAllocationService {

    private final CostAllocationProposalRepository costAllocationProposalRepository;
    private final RoomServiceChargeRepository roomServiceChargeRepository;
    private final ContractServiceRepository contractServiceRepository;
    private final CostAllocationProposalMapper costAllocationProposalMapper;

    /**
     * [P1-79] Calculates default cost split among co-residents by resident days.
     * Uses the Largest Remainder Method (Hare-Niemeyer) for VND (scale 0),
     * with deterministic tie-breaking by tenant/contract UUID.
     */
    @Override
    public JsonNode calculateDefaultAllocation(UUID roomServiceChargeId) {
        log.info("Calculating default resident-day cost allocation for chargeId={}", roomServiceChargeId);

        RoomServiceCharge charge = roomServiceChargeRepository.findById(roomServiceChargeId)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_CHARGE_NOT_FOUND));

        LocalDate chargeStart = charge.getStartDate();
        LocalDate chargeEnd = charge.getEndDate();
        BigDecimal totalAmount = charge.getTotalAmount();

        List<ContractService> activeContracts = contractServiceRepository
                .findByRoomIdAndStatus(charge.getRoomId(), ContractServiceStatus.APPROVED);

        List<TenantResidency> residencies = new ArrayList<>();
        for (ContractService cs : activeContracts) {
            LocalDate start = cs.getStartDate().isAfter(chargeStart) ? cs.getStartDate() : chargeStart;
            LocalDate end = cs.getEndDate().isBefore(chargeEnd) ? cs.getEndDate() : chargeEnd;

            if (!start.isAfter(end)) {
                long days = ChronoUnit.DAYS.between(start, end) + 1;
                if (days > 0) {
                    UUID tenantId = (cs.getContractTenantId() != null) ? cs.getContractTenantId() : cs.getRequestedBy();
                    residencies.add(TenantResidency.builder()
                            .tenantId(tenantId)
                            .contractId(cs.getContractId())
                            .contractTenantId(cs.getContractTenantId())
                            .residentDays(days)
                            .build());
                }
            }
        }

        ObjectNode result = JsonNodeFactory.instance.objectNode();
        result.put("chargeId", charge.getId().toString());
        result.put("totalAmount", totalAmount.longValue());
        result.put("method", AllocationMethod.RESIDENT_DAYS.name());

        if (residencies.isEmpty()) {
            log.warn("No active contracts found for room {} in billing range [{}, {}]",
                    charge.getRoomId(), chargeStart, chargeEnd);
            result.put("totalResidentDays", 0);
            result.set("shares", JsonNodeFactory.instance.arrayNode());
            return result;
        }

        long totalResidentDays = residencies.stream().mapToLong(TenantResidency::getResidentDays).sum();
        result.put("totalResidentDays", totalResidentDays);

        // Largest Remainder Method for exact integer allocation (0 remainder)
        List<TenantShareCalc> calculations = new ArrayList<>();
        BigInteger totalFloor = BigInteger.ZERO;

        for (TenantResidency r : residencies) {
            BigDecimal exactShare = totalAmount
                    .multiply(BigDecimal.valueOf(r.getResidentDays()))
                    .divide(BigDecimal.valueOf(totalResidentDays), 10, RoundingMode.HALF_UP);

            BigInteger floorVal = exactShare.toBigInteger();
            BigDecimal remainder = exactShare.subtract(new BigDecimal(floorVal));

            calculations.add(new TenantShareCalc(r, floorVal, remainder));
            totalFloor = totalFloor.add(floorVal);
        }

        // Remainder units to distribute (+1 VND each)
        long unitsToDistribute = totalAmount.toBigInteger().subtract(totalFloor).longValue();

        // Sort descending by remainder, then break ties deterministically by tenantId
        calculations.sort(Comparator
                .comparing(TenantShareCalc::getRemainder, Comparator.reverseOrder())
                .thenComparing(c -> c.getResidency().getTenantId()));

        for (int i = 0; i < unitsToDistribute && i < calculations.size(); i++) {
            calculations.get(i).addBonusOne();
        }

        ArrayNode sharesArray = JsonNodeFactory.instance.arrayNode();
        for (TenantShareCalc calc : calculations) {
            ObjectNode share = JsonNodeFactory.instance.objectNode();
            if (calc.getResidency().getTenantId() != null) {
                share.put("tenantId", calc.getResidency().getTenantId().toString());
            }
            if (calc.getResidency().getContractTenantId() != null) {
                share.put("contractTenantId", calc.getResidency().getContractTenantId().toString());
            }
            if (calc.getResidency().getContractId() != null) {
                share.put("contractId", calc.getResidency().getContractId().toString());
            }
            share.put("residentDays", calc.getResidency().getResidentDays());
            share.put("amount", calc.getAllocatedAmount().longValue());
            share.put("allocatedAmount", calc.getAllocatedAmount().longValue());
            sharesArray.add(share);
        }

        result.set("shares", sharesArray);
        result.set("allocations", sharesArray);
        return result;
    }

    /**
     * [P1-80] Tenant submits a cost allocation proposal.
     * Enforces [P1-82] that total allocation must match charge amount exactly.
     */
    @Override
    @Transactional
    public CostAllocationProposalResponse proposeAllocation(UUID proposedBy, CreateCostAllocationProposalRequest request) {
        log.info("Proposing cost allocation for chargeId={}, proposedBy={}",
                request.getRoomServiceChargeId(), proposedBy);

        RoomServiceCharge charge = roomServiceChargeRepository.findById(request.getRoomServiceChargeId())
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_CHARGE_NOT_FOUND));

        validateTotalAllocationMatches(request.getAllocationDetails(), charge.getTotalAmount());

        ObjectNode calculationSnapshot = JsonNodeFactory.instance.objectNode();
        calculationSnapshot.put("chargeId", charge.getId().toString());
        calculationSnapshot.put("totalAmount", charge.getTotalAmount().toPlainString());
        calculationSnapshot.put("method", request.getMethod().name());
        calculationSnapshot.put("proposedAt", Instant.now().toString());

        CostAllocationProposal proposal = CostAllocationProposal.builder()
                .roomServiceChargeId(charge.getId())
                .method(request.getMethod())
                .allocationDetails(request.getAllocationDetails())
                .calculationSnapshot(calculationSnapshot)
                .reason(request.getReason())
                .proposedBy(proposedBy)
                .status(CostAllocationProposalStatus.PENDING)
                .build();

        CostAllocationProposal saved = costAllocationProposalRepository.save(proposal);
        log.info("Saved cost allocation proposal id={} with status={}", saved.getId(), saved.getStatus());
        return costAllocationProposalMapper.toResponse(saved);
    }

    /**
     * [P1-81] Manager or owner approves or rejects a proposal.
     * [P1-83] Only approved proposals used for billing. When a new proposal is approved,
     * prior approved proposals for the same charge become SUPERSEDED.
     * [P1-84] Does not modify finalized invoice amounts on re-split requests.
     */
    @Override
    @Transactional
    public CostAllocationProposalResponse processProposal(
            UUID proposalId,
            UUID processedBy,
            ProcessCostAllocationRequest request) {

        log.info("Processing cost allocation proposal id={}, decision={}, processedBy={}",
                proposalId, request.getDecision(), processedBy);

        CostAllocationProposal proposal = costAllocationProposalRepository.findById(proposalId)
                .orElseThrow(() -> new AppException(ErrorCode.COST_ALLOCATION_PROPOSAL_NOT_FOUND));

        if (proposal.getStatus() != CostAllocationProposalStatus.PENDING) {
            throw new AppException(ErrorCode.REQUEST_FAILED,
                    "Proposal is not in PENDING state; current status: " + proposal.getStatus());
        }

        if (request.getDecision() == CostAllocationProposalStatus.APPROVED) {
            // Supersede any existing approved proposals for this charge [P1-83]
            List<CostAllocationProposal> existingApproved = costAllocationProposalRepository
                    .findByRoomServiceChargeIdAndStatus(proposal.getRoomServiceChargeId(), CostAllocationProposalStatus.APPROVED);

            for (CostAllocationProposal prev : existingApproved) {
                prev.setStatus(CostAllocationProposalStatus.SUPERSEDED);
                costAllocationProposalRepository.save(prev);
                log.info("Superseded prior proposal id={}", prev.getId());
            }

            proposal.setStatus(CostAllocationProposalStatus.APPROVED);
        } else if (request.getDecision() == CostAllocationProposalStatus.REJECTED) {
            proposal.setStatus(CostAllocationProposalStatus.REJECTED);
        } else {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Decision must be APPROVED or REJECTED");
        }

        proposal.setProcessedBy(processedBy);
        proposal.setProcessedAt(Instant.now());
        if (request.getReason() != null) {
            proposal.setReason(request.getReason());
        }

        CostAllocationProposal saved = costAllocationProposalRepository.save(proposal);
        log.info("Cost allocation proposal id={} processed as {}", saved.getId(), saved.getStatus());
        return costAllocationProposalMapper.toResponse(saved);
    }

    /**
     * Retrieves cost allocation proposal by ID.
     */
    @Override
    public CostAllocationProposalResponse getProposal(UUID proposalId) {
        CostAllocationProposal proposal = costAllocationProposalRepository.findById(proposalId)
                .orElseThrow(() -> new AppException(ErrorCode.COST_ALLOCATION_PROPOSAL_NOT_FOUND));
        return costAllocationProposalMapper.toResponse(proposal);
    }

    /**
     * Lists proposals for a specific service charge, optionally filtered by status.
     */
    @Override
    public List<CostAllocationProposalResponse> listProposals(UUID roomServiceChargeId, CostAllocationProposalStatus status) {
        List<CostAllocationProposal> list = (status != null)
                ? costAllocationProposalRepository.findByRoomServiceChargeIdAndStatus(roomServiceChargeId, status)
                : costAllocationProposalRepository.findByRoomServiceChargeId(roomServiceChargeId);

        return list.stream()
                .map(costAllocationProposalMapper::toResponse)
                .toList();
    }

    /**
     * [P1-82] Validates that the sum of allocated tenant amounts equals the charge total amount exactly.
     */
    private void validateTotalAllocationMatches(JsonNode details, BigDecimal chargeTotal) {
        if (details == null || !details.has("shares") || !details.get("shares").isArray()) {
            throw new AppException(ErrorCode.INVALID_COST_ALLOCATION,
                    "Allocation details must contain a 'shares' array");
        }

        long sumShares = 0L;
        for (JsonNode share : details.get("shares")) {
            if (share.has("amount")) {
                sumShares += share.get("amount").asLong();
            }
        }

        long expectedTotal = chargeTotal.setScale(0, RoundingMode.HALF_UP).longValue();
        if (sumShares != expectedTotal) {
            log.warn("Allocation sum mismatch: expected {}, but sum was {}", expectedTotal, sumShares);
            throw new AppException(ErrorCode.INVALID_COST_ALLOCATION,
                    "Total allocated shares (" + sumShares + ") must match charge total (" + expectedTotal + ") exactly");
        }
    }

    @Getter
    @Builder
    private static class TenantResidency {
        private final UUID tenantId;
        private final UUID contractId;
        private final UUID contractTenantId;
        private final long residentDays;
    }

    @Getter
    private static class TenantShareCalc {
        private final TenantResidency residency;
        private BigInteger allocatedAmount;
        private final BigDecimal remainder;

        public TenantShareCalc(TenantResidency residency, BigInteger floorVal, BigDecimal remainder) {
            this.residency = residency;
            this.allocatedAmount = floorVal;
            this.remainder = remainder;
        }

        public void addBonusOne() {
            this.allocatedAmount = this.allocatedAmount.add(BigInteger.ONE);
        }
    }
}
