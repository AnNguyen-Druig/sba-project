package com.sba.project.modules.identity.service.impl;

import com.sba.project.common.audit.AuditActorContext;
import com.sba.project.common.exceptions.AppException;
import com.sba.project.common.exceptions.ErrorCode;
import com.sba.project.modules.identity.dto.request.OwnerVerificationDecisionRequest;
import com.sba.project.modules.identity.dto.response.OwnerVerificationResponse;
import com.sba.project.modules.identity.entity.User;
import com.sba.project.modules.identity.enums.OwnerVerificationStatus;
import com.sba.project.modules.identity.mapper.OwnerVerificationMapperImpl;
import com.sba.project.modules.identity.repository.OwnerVerificationRepository;
import com.sba.project.modules.identity.service.OwnerVerificationAccessPolicy;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OwnerVerificationServiceImplTests {
    private static final UUID ADMIN = UUID.randomUUID();
    private static final UUID OWNER = UUID.randomUUID();

    @Mock OwnerVerificationRepository repository;
    @Mock OwnerVerificationAccessPolicy accessPolicy;
    @Mock AuditActorContext auditActor;
    OwnerVerificationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new OwnerVerificationServiceImpl(repository, new OwnerVerificationMapperImpl(), accessPolicy,
                auditActor);
    }

    @Test
    void approvalBindsAdminActorAndStoresTrimmedReason() {
        when(accessPolicy.requireAdminId()).thenReturn(ADMIN);
        when(repository.existsById(OWNER)).thenReturn(true);
        when(repository.decide(eq(OWNER), eq(OwnerVerificationStatus.APPROVED), eq(ADMIN), eq("Documents match"),
                any(Instant.class))).thenReturn(1);
        when(repository.findById(OWNER)).thenReturn(Optional.of(owner(OwnerVerificationStatus.APPROVED)));

        OwnerVerificationResponse response = service.approve(OWNER, new OwnerVerificationDecisionRequest(" Documents match "));

        verify(auditActor).bind(ADMIN);
        assertThat(response.status()).isEqualTo(OwnerVerificationStatus.APPROVED);
        assertThat(response.userId()).isEqualTo(OWNER);
        assertThat(response.documentPublicId()).isEqualTo("owner/doc");
    }

    @Test
    void rejectionUsesRejectedDecision() {
        when(accessPolicy.requireAdminId()).thenReturn(ADMIN);
        when(repository.existsById(OWNER)).thenReturn(true);
        when(repository.decide(eq(OWNER), eq(OwnerVerificationStatus.REJECTED), eq(ADMIN), eq("Blurry"),
                any(Instant.class))).thenReturn(1);
        when(repository.findById(OWNER)).thenReturn(Optional.of(owner(OwnerVerificationStatus.REJECTED)));

        assertThat(service.reject(OWNER, new OwnerVerificationDecisionRequest("Blurry")).status())
                .isEqualTo(OwnerVerificationStatus.REJECTED);
    }

    @Test
    void nonAdminIsStoppedBeforeAnyDataAccess() {
        when(accessPolicy.requireAdminId()).thenThrow(new AppException(ErrorCode.UNAUTHORIZED));

        assertError(() -> service.approve(OWNER, new OwnerVerificationDecisionRequest("x")), ErrorCode.UNAUTHORIZED);
        assertError(() -> service.list(null, PageRequest.of(0, 10)), ErrorCode.UNAUTHORIZED);
        verifyNoInteractions(repository, auditActor);
    }

    @Test
    void blankReasonIsRejected() {
        when(accessPolicy.requireAdminId()).thenReturn(ADMIN);

        assertError(() -> service.approve(OWNER, new OwnerVerificationDecisionRequest("  ")),
                ErrorCode.REVIEW_REASON_REQUIRED);
        verify(repository, never()).decide(any(), any(), any(), any(), any());
    }

    @Test
    void unknownUserAndNonPendingStatusAreDistinctErrors() {
        when(accessPolicy.requireAdminId()).thenReturn(ADMIN);
        when(repository.existsById(OWNER)).thenReturn(false);
        assertError(() -> service.approve(OWNER, new OwnerVerificationDecisionRequest("x")), ErrorCode.USER_NOT_FOUND);

        when(repository.existsById(OWNER)).thenReturn(true);
        when(repository.decide(any(), any(), any(), any(), any())).thenReturn(0);
        assertError(() -> service.approve(OWNER, new OwnerVerificationDecisionRequest("x")),
                ErrorCode.OWNER_VERIFICATION_NOT_PENDING);
    }

    @Test
    void listDefaultsToPendingAndRejectsUnsubmittedFilterAndBadSort() {
        when(accessPolicy.requireAdminId()).thenReturn(ADMIN);
        when(repository.findByOwnerVerificationStatus(eq(OwnerVerificationStatus.PENDING), any(Pageable.class)))
                .thenAnswer(call -> new PageImpl<>(List.of(owner(OwnerVerificationStatus.PENDING)),
                        call.getArgument(1), 1));

        assertThat(service.list(null, PageRequest.of(0, 10)).getContent()).hasSize(1);
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findByOwnerVerificationStatus(eq(OwnerVerificationStatus.PENDING), pageable.capture());
        assertThat(pageable.getValue().getSort()).isEqualTo(Sort.by("createdAt"));

        assertError(() -> service.list(OwnerVerificationStatus.NOT_SUBMITTED, PageRequest.of(0, 10)),
                ErrorCode.INVALID_REQUEST);
        assertError(() -> service.list(null, PageRequest.of(0, 10, Sort.by("email"))), ErrorCode.INVALID_REQUEST);
    }

    private static User owner(OwnerVerificationStatus status) {
        return User.builder().id(OWNER).fullName("Owner A").ownerVerificationStatus(status)
                .ownerDocumentPublicId("owner/doc").ownerDocumentResourceType("image").build();
    }

    private static void assertError(org.assertj.core.api.ThrowableAssert.ThrowingCallable call, ErrorCode code) {
        assertThatThrownBy(call).isInstanceOf(AppException.class)
                .extracting(e -> ((AppException) e).getErrorCode()).isEqualTo(code);
    }
}
