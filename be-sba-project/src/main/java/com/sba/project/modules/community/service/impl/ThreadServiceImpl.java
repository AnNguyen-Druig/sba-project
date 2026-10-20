package com.sba.project.modules.community.service.impl;

import com.sba.project.common.base.PagedResponse;
import com.sba.project.common.exceptions.AppException;
import com.sba.project.common.exceptions.ErrorCode;
import com.sba.project.common.security.CurrentUserProvider;
import com.sba.project.modules.community.dto.request.ThreadRequest;
import com.sba.project.modules.community.dto.response.ThreadResponse;
import com.sba.project.modules.community.entity.Thread;
import com.sba.project.modules.community.enums.ThreadStatus;
import com.sba.project.modules.community.mapper.ThreadMapper;
import com.sba.project.modules.community.repository.ThreadRepository;
import com.sba.project.modules.community.service.ThreadService;
import com.sba.project.modules.community.specifications.ThreadSpecifications;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Only authenticated users reach any operation (P3-03: Guests cannot view Threads). The expiry period
 * is a system setting (app.threads.expiry-days); the project has not fixed a value, so the default 0
 * means threads never expire on their own.
 */
@Service
public class ThreadServiceImpl implements ThreadService {
    private static final Set<String> SORTABLE_PROPERTIES = Set.of("createdAt", "updatedAt", "title");
    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "createdAt");

    private final ThreadRepository threadRepository;
    private final ThreadMapper mapper;
    private final CurrentUserProvider currentUser;
    private final JsonMapper jsonMapper;
    private final int expiryDays;

    public ThreadServiceImpl(ThreadRepository threadRepository, ThreadMapper mapper, CurrentUserProvider currentUser,
            JsonMapper jsonMapper, @Value("${app.threads.expiry-days:0}") int expiryDays) {
        this.threadRepository = threadRepository;
        this.mapper = mapper;
        this.currentUser = currentUser;
        this.jsonMapper = jsonMapper;
        this.expiryDays = Math.max(expiryDays, 0);
    }

    @Override
    @Transactional
    public ThreadResponse create(ThreadRequest request) {
        UUID actorId = currentUser.currentUserId();
        JsonNode criteria = criteriaJson(request);
        Instant expiresAt = expiryDays > 0 ? Instant.now().plus(Duration.ofDays(expiryDays)) : null;
        Thread thread = threadRepository.save(Thread.builder().authorUserId(actorId)
                .title(request.title().trim()).description(request.description().trim()).criteria(criteria)
                .status(ThreadStatus.OPEN).expiresAt(expiresAt).build());
        return mapper.toResponse(thread);
    }

    @Override
    @Transactional(readOnly = true)
    public ThreadResponse get(UUID threadId) {
        UUID actorId = currentUser.currentUserId();
        Thread thread = load(threadId);
        // A HIDDEN thread (moderated by ADMIN) is visible to its author only.
        if (thread.getStatus() == ThreadStatus.HIDDEN && !actorId.equals(thread.getAuthorUserId())) {
            throw new AppException(ErrorCode.THREAD_NOT_FOUND);
        }
        return mapper.toResponse(thread);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ThreadResponse> listMine(ThreadStatus status, Pageable pageable) {
        UUID actorId = currentUser.currentUserId();
        return page(ThreadSpecifications.ownedBy(actorId, status), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ThreadResponse> browse(String keyword, Pageable pageable) {
        currentUser.currentUserId();
        return page(ThreadSpecifications.browsable(keyword), pageable);
    }

    @Override
    @Transactional
    public ThreadResponse update(UUID threadId, ThreadRequest request) {
        Thread thread = loadOwnedOpen(threadId);
        JsonNode criteria = criteriaJson(request);
        if (threadRepository.updateOpen(threadId, request.title().trim(), request.description().trim(), criteria,
                Instant.now()) == 0) {
            throw new AppException(ErrorCode.THREAD_STATE_CONFLICT);
        }
        return mapper.toResponse(load(thread.getId()));
    }

    @Override
    @Transactional
    public ThreadResponse close(UUID threadId) {
        loadOwnedOpen(threadId);
        if (threadRepository.closeOpen(threadId, Instant.now()) == 0) {
            throw new AppException(ErrorCode.THREAD_STATE_CONFLICT);
        }
        return mapper.toResponse(load(threadId));
    }

    @Override
    @Transactional
    public int closeExpiredThreads() {
        return threadRepository.closeExpired(Instant.now());
    }

    private Thread loadOwnedOpen(UUID threadId) {
        UUID actorId = currentUser.currentUserId();
        Thread thread = load(threadId);
        if (!actorId.equals(thread.getAuthorUserId())) throw new AppException(ErrorCode.UNAUTHORIZED);
        if (thread.getStatus() != ThreadStatus.OPEN) throw new AppException(ErrorCode.THREAD_STATE_CONFLICT);
        return thread;
    }

    private Thread load(UUID threadId) {
        return threadRepository.findById(threadId).orElseThrow(() -> new AppException(ErrorCode.THREAD_NOT_FOUND));
    }

    private JsonNode criteriaJson(ThreadRequest request) {
        if (!request.criteria().hasValidPriceRange()) throw new AppException(ErrorCode.INVALID_REQUEST);
        return jsonMapper.valueToTree(request.criteria());
    }

    private PagedResponse<ThreadResponse> page(Specification<Thread> specification, Pageable pageable) {
        Page<Thread> page = threadRepository.findAll(specification, withAllowedSort(pageable));
        return PagedResponse.of(page.getContent().stream().map(mapper::toResponse).toList(),
                page.getNumber() + 1, page.getSize(), page.getTotalElements());
    }

    private Pageable withAllowedSort(Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), DEFAULT_SORT);
        }
        for (Sort.Order order : pageable.getSort()) {
            if (!SORTABLE_PROPERTIES.contains(order.getProperty())) throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        return pageable;
    }
}
