package com.sba.project.service;

import com.sba.project.repository.InvalidateTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Component
@RequiredArgsConstructor
public class InvalidateTokenCleanup {

    private final InvalidateTokenRepository invalidateTokenRepository;

    @Scheduled(cron = "0 0 3 * * *")
    public void removeExpiredTokens() {
        invalidateTokenRepository.deleteAllByExpiredTimeBefore(LocalDateTime.now(ZoneOffset.UTC));
    }
}
