package com.sba.project.common.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RefreshTokenServiceTests {
    @Test
    void issuesRandomRefreshTokenAndPersistsOnlyItsHash() {
        RefreshTokenService service = new RefreshTokenService(604_800_000);
        UUID userId = UUID.randomUUID();

        var first = service.issue(userId);
        var second = service.issue(userId);

        assertThat(service.getUserId(first.value())).isEqualTo(userId);
        assertThat(first.value()).isNotEqualTo(second.value());
        assertThat(first.hash()).matches("[0-9a-f]{64}");
        assertThat(service.matches(first.hash(), first.value())).isTrue();
        assertThat(service.matches(first.hash(), first.value() + "tampered")).isFalse();
        assertThat(first.expiresAt()).isAfter(Instant.now());
    }

    @Test
    void rejectsMalformedTokensAndStoredHashes() {
        RefreshTokenService service = new RefreshTokenService(604_800_000);

        assertThat(service.getUserId(null)).isNull();
        assertThat(service.getUserId("not-a-token")).isNull();
        assertThat(service.getUserId(UUID.randomUUID() + ".short")).isNull();
        assertThat(service.matches("not-a-hash", "token")).isFalse();
    }
}
