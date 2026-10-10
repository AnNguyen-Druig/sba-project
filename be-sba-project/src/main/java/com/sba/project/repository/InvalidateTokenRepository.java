package com.sba.project.repository;

import com.sba.project.entity.InvalidateToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface InvalidateTokenRepository extends JpaRepository<InvalidateToken, String> {

    void deleteAllByExpiredTimeBefore(LocalDateTime expiredTime);
}
