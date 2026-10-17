package com.sba.project.modules.identity.repository;

import com.sba.project.modules.identity.entity.User;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, UUID id);

    boolean existsByPhone(String phone);

    boolean existsByPhoneAndIdNot(String phone, UUID id);

    Optional<User> findByEmailIgnoreCase(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.verificationSecretHash = :secretHash")
    Optional<User> findByVerificationSecretHashForUpdate(@Param("secretHash") String secretHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id = :id")
    Optional<User> findByIdForUpdate(@Param("id") UUID id);

    @Modifying
    @Transactional
    @Query("""
            update User u
            set u.refreshTokenHash = null, u.refreshTokenExpiresAt = null
            where u.refreshTokenExpiresAt <= :cutoff
            """)
    int clearExpiredRefreshTokens(@Param("cutoff") Instant cutoff);
}
