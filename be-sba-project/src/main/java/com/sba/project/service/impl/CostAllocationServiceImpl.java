package com.sba.project.service.impl;

import com.sba.project.dto.request.*;
import com.sba.project.dto.response.*;
import com.sba.project.entity.*;
import com.sba.project.enums.AllocationStatus;
import com.sba.project.exception.*;
import com.sba.project.repository.*;
import com.sba.project.service.CostAllocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CostAllocationServiceImpl implements CostAllocationService {

    private final CostAllocationProposalRepository proposalRepository;
    private final RoomServiceChargeRepository chargeRepository;

    @Override
    public CostAllocationResponse createProposal(CreateCostAllocationRequest request, UUID proposedBy) {
        RoomServiceCharge charge = chargeRepository.findById(request.getRoomServiceChargeId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phí dịch vụ: " + request.getRoomServiceChargeId()));

        // [P1-84] Không cho thay đổi khi hóa đơn đã chốt
        if (charge.isFinalized()) {
            // Kiểm tra xem đã có allocation duyệt chưa
            proposalRepository.findByRoomServiceChargeIdAndStatus(charge.getId(), AllocationStatus.APPROVED)
                    .ifPresent(p -> {
                        throw new BusinessException("Phí đã có phương án phân bổ được duyệt. Không thể thay đổi khi đã chốt.");
                    });
        }

        // [P1-82] Kiểm tra tổng phân bổ phải khớp phí gốc
        BigDecimal totalAllocated = request.getItems().stream()
                .map(CreateCostAllocationRequest.AllocationItemRequest::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalAllocated.compareTo(charge.getTotalAmount()) != 0) {
            throw new BusinessException(
                String.format("Tổng phân bổ (%s) không khớp với tổng phí dịch vụ (%s)",
                    totalAllocated.toPlainString(), charge.getTotalAmount().toPlainString()));
        }

        CostAllocationProposal proposal = CostAllocationProposal.builder()
                .roomServiceCharge(charge)
                .roomId(request.getRoomId())
                .status(AllocationStatus.PENDING)
                .reason(request.getReason())
                .proposedBy(proposedBy)
                .items(new ArrayList<>())
                .build();

        for (CreateCostAllocationRequest.AllocationItemRequest itemReq : request.getItems()) {
            CostAllocationItem item = CostAllocationItem.builder()
                    .proposal(proposal)
                    .tenantId(itemReq.getTenantId())
                    .amount(itemReq.getAmount())
                    .percentage(itemReq.getPercentage())
                    .note(itemReq.getNote())
                    .build();
            proposal.getItems().add(item);
        }

        proposal = proposalRepository.save(proposal);
        log.info("Created cost allocation proposal {} for charge {} by tenant {}", proposal.getId(), charge.getId(), proposedBy);
        return mapToResponse(proposal);
    }

    @Override
    public CostAllocationResponse processProposal(UUID proposalId, ProcessAllocationRequest request, UUID processedBy) {
        CostAllocationProposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đề xuất phân bổ: " + proposalId));

        if (proposal.getStatus() != AllocationStatus.PENDING) {
            throw new BusinessException("Chỉ có thể xử lý đề xuất đang chờ duyệt");
        }

        if (Boolean.TRUE.equals(request.getApproved())) {
            proposal.setStatus(AllocationStatus.APPROVED);
        } else {
            proposal.setStatus(AllocationStatus.REJECTED);
            proposal.setRejectionReason(request.getRejectionReason());
        }
        proposal.setProcessedBy(processedBy);
        proposal.setProcessedAt(LocalDateTime.now());

        proposal = proposalRepository.save(proposal);
        log.info("Proposal {} {} by {}", proposalId, request.getApproved() ? "approved" : "rejected", processedBy);
        return mapToResponse(proposal);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CostAllocationResponse> getProposalsByRoom(UUID roomId, AllocationStatus status, Pageable pageable) {
        Page<CostAllocationProposal> page;
        if (status != null) {
            page = proposalRepository.findByRoomIdAndStatus(roomId, status, pageable);
        } else {
            page = proposalRepository.findByRoomId(roomId, pageable);
        }
        return buildPageResponse(page.map(this::mapToResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CostAllocationResponse> getProposalsByTenant(UUID tenantId) {
        return proposalRepository.findByProposedBy(tenantId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CostAllocationResponse> getProposalsByCharge(UUID chargeId) {
        return proposalRepository.findByRoomServiceChargeId(chargeId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public CostAllocationResponse createDefaultAllocation(UUID chargeId, List<UUID> tenantIds, UUID createdBy) {
        // [P1-79] Mặc định chia đều
        RoomServiceCharge charge = chargeRepository.findById(chargeId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phí dịch vụ: " + chargeId));

        if (tenantIds == null || tenantIds.isEmpty()) {
            throw new BusinessException("Danh sách Tenant không được rỗng");
        }

        BigDecimal totalAmount = charge.getTotalAmount();
        int count = tenantIds.size();
        BigDecimal perPerson = totalAmount.divide(BigDecimal.valueOf(count), 0, RoundingMode.FLOOR);
        BigDecimal remainder = totalAmount.subtract(perPerson.multiply(BigDecimal.valueOf(count)));

        CostAllocationProposal proposal = CostAllocationProposal.builder()
                .roomServiceCharge(charge)
                .roomId(charge.getRoomId())
                .status(AllocationStatus.PENDING)
                .reason("Phân bổ mặc định - chia đều")
                .proposedBy(createdBy)
                .items(new ArrayList<>())
                .build();

        BigDecimal percentage = BigDecimal.valueOf(100).divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);

        for (int i = 0; i < tenantIds.size(); i++) {
            BigDecimal amount = perPerson;
            // Phần dư VND cộng cho người đầu tiên
            if (i == 0) {
                amount = amount.add(remainder);
            }

            CostAllocationItem item = CostAllocationItem.builder()
                    .proposal(proposal)
                    .tenantId(tenantIds.get(i))
                    .amount(amount)
                    .percentage(percentage)
                    .note("Chia đều")
                    .build();
            proposal.getItems().add(item);
        }

        proposal = proposalRepository.save(proposal);
        log.info("Created default allocation for charge {} with {} tenants by {}", chargeId, count, createdBy);
        return mapToResponse(proposal);
    }

    // ==================== Mappers ====================

    private CostAllocationResponse mapToResponse(CostAllocationProposal p) {
        List<CostAllocationResponse.AllocationItemResponse> items = p.getItems().stream()
                .map(item -> CostAllocationResponse.AllocationItemResponse.builder()
                        .id(item.getId())
                        .tenantId(item.getTenantId())
                        .amount(item.getAmount())
                        .percentage(item.getPercentage())
                        .note(item.getNote())
                        .build())
                .collect(Collectors.toList());

        return CostAllocationResponse.builder()
                .id(p.getId())
                .roomServiceChargeId(p.getRoomServiceCharge().getId())
                .roomId(p.getRoomId())
                .status(p.getStatus())
                .reason(p.getReason())
                .proposedBy(p.getProposedBy())
                .processedBy(p.getProcessedBy())
                .processedAt(p.getProcessedAt())
                .rejectionReason(p.getRejectionReason())
                .items(items)
                .createdAt(p.getCreatedAt())
                .build();
    }

    private <T> PageResponse<T> buildPageResponse(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
