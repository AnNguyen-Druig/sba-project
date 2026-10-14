package com.sba.project.modules.community.service.impl;

import com.sba.project.common.exceptions.AppException;
import com.sba.project.common.exceptions.ErrorCode;
import com.sba.project.common.security.CurrentUserProvider;
import com.sba.project.modules.community.dto.request.ThreadCriteria;
import com.sba.project.modules.community.dto.request.ThreadRequest;
import com.sba.project.modules.community.dto.response.ThreadResponse;
import com.sba.project.modules.community.entity.Thread;
import com.sba.project.modules.community.enums.ThreadStatus;
import com.sba.project.modules.community.mapper.ThreadMapperImpl;
import com.sba.project.modules.community.repository.ThreadRepository;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
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
import org.springframework.data.jpa.domain.Specification;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ThreadServiceImplTests {
    private static final UUID AUTHOR = UUID.randomUUID();

    @Mock ThreadRepository threadRepository;
    @Mock CurrentUserProvider currentUser;
    ThreadServiceImpl service;

    @BeforeEach
    void setUp() {
        service = newService(0);
    }

    private ThreadServiceImpl newService(int expiryDays) {
        return new ThreadServiceImpl(threadRepository, new ThreadMapperImpl(), currentUser,
                JsonMapper.builder().build(), expiryDays);
    }

    @Test
    void createStoresOpenThreadWithCriteriaJsonAndNoExpiryByDefault() {
        when(currentUser.currentUserId()).thenReturn(AUTHOR);
        when(threadRepository.save(any(Thread.class))).thenAnswer(call -> call.getArgument(0));

        ThreadResponse response = service.create(request(new ThreadCriteria("Thu Duc", new BigDecimal("2000000"),
                new BigDecimal("3000000"), null, 2, List.of("wifi"), LocalDate.of(2027, 1, 1))));

        ArgumentCaptor<Thread> saved = ArgumentCaptor.forClass(Thread.class);
        verify(threadRepository).save(saved.capture());
        assertThat(saved.getValue().getAuthorUserId()).isEqualTo(AUTHOR);
        assertThat(saved.getValue().getStatus()).isEqualTo(ThreadStatus.OPEN);
        assertThat(saved.getValue().getExpiresAt()).isNull();
        JsonNode criteria = saved.getValue().getCriteria();
        assertThat(criteria.get("area").asString()).isEqualTo("Thu Duc");
        assertThat(criteria.get("capacity").asInt()).isEqualTo(2);
        assertThat(criteria.has("minAreaSqm")).isFalse();
        assertThat(response.status()).isEqualTo(ThreadStatus.OPEN);
    }

    @Test
    void configuredExpiryDaysSetExpiresAt() {
        service = newService(30);
        when(currentUser.currentUserId()).thenReturn(AUTHOR);
        when(threadRepository.save(any(Thread.class))).thenAnswer(call -> call.getArgument(0));

        service.create(request(new ThreadCriteria(null, null, null, null, null, null, null)));

        ArgumentCaptor<Thread> saved = ArgumentCaptor.forClass(Thread.class);
        verify(threadRepository).save(saved.capture());
        assertThat(saved.getValue().getExpiresAt()).isBetween(Instant.now().plus(Duration.ofDays(30)).minusSeconds(60),
                Instant.now().plus(Duration.ofDays(30)).plusSeconds(60));
    }

    @Test
    void invertedPriceRangeIsRejected() {
        when(currentUser.currentUserId()).thenReturn(AUTHOR);

        assertError(() -> service.create(request(new ThreadCriteria(null, new BigDecimal("5"), new BigDecimal("1"),
                null, null, null, null))), ErrorCode.INVALID_REQUEST);
        verify(threadRepository, never()).save(any());
    }

    @Test
    void unauthenticatedCallersAreStoppedOnEveryOperation() {
        when(currentUser.currentUserId()).thenThrow(new AppException(ErrorCode.UNAUTHENTICATED));
        UUID id = UUID.randomUUID();

        assertError(() -> service.create(request(emptyCriteria())), ErrorCode.UNAUTHENTICATED);
        assertError(() -> service.get(id), ErrorCode.UNAUTHENTICATED);
        assertError(() -> service.browse(null, PageRequest.of(0, 10)), ErrorCode.UNAUTHENTICATED);
        assertError(() -> service.listMine(null, PageRequest.of(0, 10)), ErrorCode.UNAUTHENTICATED);
        assertError(() -> service.update(id, request(emptyCriteria())), ErrorCode.UNAUTHENTICATED);
        assertError(() -> service.close(id), ErrorCode.UNAUTHENTICATED);
        verify(threadRepository, never()).save(any());
    }

    @Test
    void hiddenThreadIsInvisibleToOthersButVisibleToItsAuthor() {
        Thread hidden = thread(ThreadStatus.HIDDEN);
        when(threadRepository.findById(hidden.getId())).thenReturn(Optional.of(hidden));

        when(currentUser.currentUserId()).thenReturn(UUID.randomUUID());
        assertError(() -> service.get(hidden.getId()), ErrorCode.THREAD_NOT_FOUND);

        when(currentUser.currentUserId()).thenReturn(AUTHOR);
        assertThat(service.get(hidden.getId()).status()).isEqualTo(ThreadStatus.HIDDEN);
    }

    @Test
    void closedThreadIsReadableByAnyLoggedInUser() {
        Thread closed = thread(ThreadStatus.CLOSED);
        when(currentUser.currentUserId()).thenReturn(UUID.randomUUID());
        when(threadRepository.findById(closed.getId())).thenReturn(Optional.of(closed));

        assertThat(service.get(closed.getId()).status()).isEqualTo(ThreadStatus.CLOSED);
    }

    @Test
    void authorUpdatesOwnOpenThroughConditionalUpdate() {
        Thread open = thread(ThreadStatus.OPEN);
        when(currentUser.currentUserId()).thenReturn(AUTHOR);
        when(threadRepository.findById(open.getId())).thenReturn(Optional.of(open));
        when(threadRepository.updateOpen(eq(open.getId()), eq("New title"), eq("New description"), any(JsonNode.class),
                any(Instant.class))).thenReturn(1);

        service.update(open.getId(), new ThreadRequest(" New title ", " New description ", emptyCriteria()));

        verify(threadRepository).updateOpen(eq(open.getId()), eq("New title"), eq("New description"),
                any(JsonNode.class), any(Instant.class));
    }

    @Test
    void onlyTheAuthorMayEditOrClose() {
        Thread open = thread(ThreadStatus.OPEN);
        when(currentUser.currentUserId()).thenReturn(UUID.randomUUID());
        when(threadRepository.findById(open.getId())).thenReturn(Optional.of(open));

        assertError(() -> service.update(open.getId(), request(emptyCriteria())), ErrorCode.UNAUTHORIZED);
        assertError(() -> service.close(open.getId()), ErrorCode.UNAUTHORIZED);
        verify(threadRepository, never()).closeOpen(any(), any());
    }

    @Test
    void closedOrHiddenThreadsCannotBeEditedOrClosedAgain() {
        for (ThreadStatus status : List.of(ThreadStatus.CLOSED, ThreadStatus.HIDDEN)) {
            Thread thread = thread(status);
            when(currentUser.currentUserId()).thenReturn(AUTHOR);
            when(threadRepository.findById(thread.getId())).thenReturn(Optional.of(thread));

            assertError(() -> service.update(thread.getId(), request(emptyCriteria())), ErrorCode.THREAD_STATE_CONFLICT);
            assertError(() -> service.close(thread.getId()), ErrorCode.THREAD_STATE_CONFLICT);
        }
    }

    @Test
    void losingTheRaceAgainstModerationIsAStateConflict() {
        Thread open = thread(ThreadStatus.OPEN);
        when(currentUser.currentUserId()).thenReturn(AUTHOR);
        when(threadRepository.findById(open.getId())).thenReturn(Optional.of(open));
        when(threadRepository.closeOpen(eq(open.getId()), any(Instant.class))).thenReturn(0);

        assertError(() -> service.close(open.getId()), ErrorCode.THREAD_STATE_CONFLICT);
    }

    @Test
    void unknownThreadIsNotFound() {
        UUID id = UUID.randomUUID();
        when(currentUser.currentUserId()).thenReturn(AUTHOR);
        when(threadRepository.findById(id)).thenReturn(Optional.empty());

        assertError(() -> service.get(id), ErrorCode.THREAD_NOT_FOUND);
    }

    @Test
    void listsUseNewestFirstDefaultAndRejectUnknownSort() {
        when(currentUser.currentUserId()).thenReturn(AUTHOR);
        when(threadRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenAnswer(call -> new PageImpl<>(List.of(thread(ThreadStatus.OPEN)), call.getArgument(1), 1));

        var page = service.listMine(null, PageRequest.of(0, 10));
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(threadRepository).findAll(any(Specification.class), pageable.capture());
        assertThat(pageable.getValue().getSort()).isEqualTo(Sort.by(Sort.Direction.DESC, "createdAt"));
        assertThat(page.getNumber()).isEqualTo(1);

        assertError(() -> service.browse(null, PageRequest.of(0, 10, Sort.by("authorUserId"))),
                ErrorCode.INVALID_REQUEST);
    }

    @Test
    void autoCloseDelegatesToConditionalExpiryUpdate() {
        when(threadRepository.closeExpired(any(Instant.class))).thenReturn(3);

        assertThat(service.closeExpiredThreads()).isEqualTo(3);
    }

    private static ThreadCriteria emptyCriteria() {
        return new ThreadCriteria(null, null, null, null, null, null, null);
    }

    private static ThreadRequest request(ThreadCriteria criteria) {
        return new ThreadRequest("Looking for a room", "Near campus", criteria);
    }

    private static Thread thread(ThreadStatus status) {
        return Thread.builder().id(UUID.randomUUID()).authorUserId(AUTHOR).title("t").description("d")
                .criteria(JsonMapper.builder().build().createObjectNode()).status(status).build();
    }

    private static void assertError(org.assertj.core.api.ThrowableAssert.ThrowingCallable call, ErrorCode code) {
        assertThatThrownBy(call).isInstanceOf(AppException.class)
                .extracting(e -> ((AppException) e).getErrorCode()).isEqualTo(code);
    }
}
