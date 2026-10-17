package com.sba.project.modules.identity.schedule;

import com.sba.project.modules.identity.repository.InvalidatedTokenRepository;
import com.sba.project.modules.identity.repository.UserRepository;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@EnableScheduling
public class ExpiredTokenCleanupJob {
    private static final Logger log = LoggerFactory.getLogger(ExpiredTokenCleanupJob.class);

    private final InvalidatedTokenRepository invalidatedTokenRepository;
    private final UserRepository userRepository;

    public ExpiredTokenCleanupJob(
            InvalidatedTokenRepository invalidatedTokenRepository,
            UserRepository userRepository) {
        this.invalidatedTokenRepository = invalidatedTokenRepository;
        this.userRepository = userRepository;
    }

    @Scheduled(
            initialDelayString = "${app.jwt.cleanup-initial-delay-ms:3600000}",
            fixedDelayString = "${app.jwt.cleanup-interval-ms:3600000}")
    @Transactional
    public void cleanupExpiredTokens() {
        Instant now = Instant.now();
        int revokedAccessTokens = invalidatedTokenRepository.deleteExpiredBefore(
                LocalDateTime.ofInstant(now, ZoneOffset.UTC));
        int expiredRefreshTokens = userRepository.clearExpiredRefreshTokens(now);
        if (revokedAccessTokens > 0 || expiredRefreshTokens > 0) {
            log.info("Removed {} expired revoked access tokens and {} expired refresh tokens",
                    revokedAccessTokens, expiredRefreshTokens);
        }
    }
}
