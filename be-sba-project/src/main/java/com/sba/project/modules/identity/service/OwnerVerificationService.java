package com.sba.project.modules.identity.service;

import com.sba.project.common.base.PagedResponse;
import com.sba.project.modules.identity.dto.request.OwnerVerificationDecisionRequest;
import com.sba.project.modules.identity.dto.response.OwnerVerificationResponse;
import com.sba.project.modules.identity.enums.OwnerVerificationStatus;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

/** BE-06.1 ADMIN review of Owner verification (P1-08, P1-132). */
public interface OwnerVerificationService {
    PagedResponse<OwnerVerificationResponse> list(OwnerVerificationStatus status, Pageable pageable);

    OwnerVerificationResponse approve(UUID userId, OwnerVerificationDecisionRequest request);

    OwnerVerificationResponse reject(UUID userId, OwnerVerificationDecisionRequest request);
}
