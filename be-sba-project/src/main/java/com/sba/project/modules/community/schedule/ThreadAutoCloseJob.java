package com.sba.project.modules.community.schedule;

import com.sba.project.modules.community.service.ThreadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** P3-14 background job: closes OPEN threads past their expiry. Interval is configurable. */
@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class ThreadAutoCloseJob {
    private final ThreadService threadService;

    @Scheduled(fixedDelayString = "${app.threads.auto-close-interval-ms:600000}",
            initialDelayString = "${app.threads.auto-close-initial-delay-ms:60000}")
    public void closeExpiredThreads() {
        try {
            int closed = threadService.closeExpiredThreads();
            if (closed > 0) log.info("Auto-closed {} expired threads", closed);
        } catch (RuntimeException ex) {
            // Never let one failed run cancel the schedule; the next tick retries.
            log.warn("Thread auto-close run failed ({})", ex.getClass().getSimpleName());
        }
    }
}
