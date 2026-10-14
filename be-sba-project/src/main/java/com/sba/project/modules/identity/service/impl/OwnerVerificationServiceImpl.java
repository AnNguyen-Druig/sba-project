package com.sba.project.modules.identity.service.impl;

import com.sba.project.common.audit.AuditActorContext;
import com.sba.project.common.base.PagedResponse;
import com.sba.project.common.exceptions.AppException;
import com.sba.project.common.exceptions.ErrorCode;
import com.sba.project.modules.identity.dto.request.OwnerVerificationDecisionRequest;
import com.sba.project.modules.identity.dto.response.OwnerVerificationResponse;
import com.sba.project.modules.identity.entity.User;
import com.sba.project.modules.identity.enums.OwnerVerificationStatus;
import com.sba.project.modules.identity.mapper.OwnerVerificationMapper;
import com.sba.project.modules.identity.repository.OwnerVerificationRepository;
import com.sba.project.modules.identity.service.OwnerVerificationAccessPolicy;
import com.sba.project.modules.identity.service.OwnerVerificationService;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Review only. Submitting documents is not a Student 6 feature, and granting operating rights after
 * approval is an open team decision (all-feature.txt section 5), so no role is changed here.
 */
@Service
@RequiredArgsConstructor
public class OwnerVerificationServiceImpl implements OwnerVerificationService {
    private static final Set<String> SORTABLE_PROPERTIES = Set.of("fullName", "ownerReviewedAt", "createdAt");
    private static final Set<OwnerVerificationStatus> LISTABLE =
            EnumSet.of(OwnerVerificationStatus.PENDING, OwnerVerificationStatus.APPROVED,
                    OwnerVerificationStatus.REJECTED);

    private final OwnerVerificationRepository repository;
    private final OwnerVerificationMapper mapper;
    private final OwnerVerificationAccessPolicy accessPolicy;
    private final AuditActorContext auditActor;

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<OwnerVerificationResponse> list(OwnerVerificationStatus status, Pageable pageable) {
        accessPolicy.requireAdminId();
        OwnerVerificationStatus filter = status == null ? OwnerVerificationStatus.PENDING : status;
        if (!LISTABLE.contains(filter)) throw new AppException(ErrorCode.INVALID_REQUEST);
        Page<User> page = repository.findByOwnerVerificationStatus(filter, withAllowedSort(pageable));
        return PagedResponse.of(page.getContent().stream().map(mapper::toResponse).toList(),
                page.getNumber() + 1, page.getSize(), page.getTotalElements());
    }

    @Override
    @Transactional
    public OwnerVerificationResponse approve(UUID userId, OwnerVerificationDecisionRequest request) {
        return decide(userId, request, OwnerVerificationStatus.APPROVED);
    }

    @Override
    @Transactional
    public OwnerVerificationResponse reject(UUID userId, OwnerVerificationDecisionRequest request) {
        return decide(userId, request, OwnerVerificationStatus.REJECTED);
    }

    private OwnerVerificationResponse decide(UUID userId, OwnerVerificationDecisionRequest request,
            OwnerVerificationStatus decision) {
        UUID adminId = accessPolicy.requireAdminId();
        if (request == null || request.reason() == null || request.reason().isBlank()) {
            throw new AppException(ErrorCode.REVIEW_REASON_REQUIRED);
        }
        if (!repository.existsById(userId)) throw new AppException(ErrorCode.USER_NOT_FOUND);
        auditActor.bind(adminId);
        if (repository.decide(userId, decision, adminId, request.reason().trim(), Instant.now()) == 0) {
            throw new AppException(ErrorCode.OWNER_VERIFICATION_NOT_PENDING);
        }
        return mapper.toResponse(repository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND)));
    }

    private Pageable withAllowedSort(Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("createdAt"));
        }
        for (Sort.Order order : pageable.getSort()) {
            if (!SORTABLE_PROPERTIES.contains(order.getProperty())) throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        return pageable;
    }
}
