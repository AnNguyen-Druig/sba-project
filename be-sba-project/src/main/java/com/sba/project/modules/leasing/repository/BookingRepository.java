package com.sba.project.modules.leasing.repository;

import com.sba.project.modules.leasing.entity.Booking;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<Booking, UUID>, JpaSpecificationExecutor<Booking> {
    /** {@code version} is a scalar revision, so state changes serialize on a row lock instead. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Booking b where b.id = :id")
    Optional<Booking> findByIdForUpdate(@Param("id") UUID id);

    /** Open holds of a Room whose deadline has passed (BR-P10a, BR-P11). */
    @Query("""
            select b from Booking b
            where b.roomId = :roomId
              and ((b.status = com.sba.project.modules.leasing.enums.BookingStatus.AWAITING_PAYMENT
                    and b.temporaryHoldUntil < :now)
                or (b.status = com.sba.project.modules.leasing.enums.BookingStatus.CONFIRMED
                    and b.confirmedHoldUntil < :now))
            """)
    List<Booking> findStaleHoldsInRoom(@Param("roomId") UUID roomId, @Param("now") Instant now);
}
