package com.sba.project.modules.community.service;

import com.sba.project.common.base.PagedResponse;
import com.sba.project.modules.community.dto.request.ThreadRequest;
import com.sba.project.modules.community.dto.response.ThreadResponse;
import com.sba.project.modules.community.enums.ThreadStatus;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

/** BE-06.9 Thread basics (P3-01, P3-02, P3-03, P3-14). Comments are DEFERRED (P3-04) and not modelled. */
public interface ThreadService {
    ThreadResponse create(ThreadRequest request);

    ThreadResponse get(UUID threadId);

    PagedResponse<ThreadResponse> listMine(ThreadStatus status, Pageable pageable);

    PagedResponse<ThreadResponse> browse(String keyword, Pageable pageable);

    ThreadResponse update(UUID threadId, ThreadRequest request);

    ThreadResponse close(UUID threadId);

    /** P3-14: closes OPEN threads whose expiry passed; returns how many were closed. */
    int closeExpiredThreads();
}
