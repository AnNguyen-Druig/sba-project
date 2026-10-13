package com.sba.project.modules.identity.repository;

import com.sba.project.modules.identity.entity.InvalidatedToken;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, String> {
    @Modifying
    @Transactional
    @Query("delete from InvalidatedToken token where token.expiredTime <= :cutoff")
    int deleteExpiredBefore(@Param("cutoff") LocalDateTime cutoff);

    default boolean isInvalidated(String tokenId) {
        return existsById(tokenId);
    }

    default void invalidate(String tokenId, Instant expiresAt) {
        saveAndFlush(new InvalidatedToken(
                tokenId,
                LocalDateTime.ofInstant(expiresAt, ZoneOffset.UTC)));
    }
}
