package com.sba.project.modules.identity.schedule;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.sba.project.modules.identity.repository.InvalidatedTokenRepository;
import com.sba.project.modules.identity.repository.UserRepository;
import java.time.Instant;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ExpiredTokenCleanupJobTests {
    @Test
    void removesExpiredRevocationsAndExpiredRefreshTokenHashes() {
        InvalidatedTokenRepository invalidatedTokenRepository = mock(InvalidatedTokenRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        ExpiredTokenCleanupJob job = new ExpiredTokenCleanupJob(invalidatedTokenRepository, userRepository);

        job.cleanupExpiredTokens();

        verify(invalidatedTokenRepository).deleteExpiredBefore(any(LocalDateTime.class));
        verify(userRepository).clearExpiredRefreshTokens(any(Instant.class));
    }
}
