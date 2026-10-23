package com.sba.project.modules.leasing.repository;

import com.sba.project.modules.leasing.entity.Booking;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
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

    String LAPSED_HOLD = """
              ((b.status = com.sba.project.modules.leasing.enums.BookingStatus.AWAITING_PAYMENT
                    and b.temporaryHoldUntil < :now)
                or (b.status = com.sba.project.modules.leasing.enums.BookingStatus.CONFIRMED
                    and b.confirmedHoldUntil < :now))
            """;

    /**
     * Open holds of a Room whose deadline has passed (BR-P10a, BR-P11), row-locked so they cannot be
     * confirmed and expired at the same time.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Booking b where b.roomId = :roomId and " + LAPSED_HOLD)
    List<Booking> findStaleHoldsInRoom(@Param("roomId") UUID roomId, @Param("now") Instant now);

    @Query("select b.roomId from Booking b where b.id = :id")
    Optional<UUID> findRoomIdById(@Param("id") UUID id);

    /** Payment reference lookup: {@code hexPrefix} is lower-case hex of the id without dashes, plus '%'. */
    @Query(value = "select id from rental.bookings where replace(cast(id as text), '-', '') like :hexPrefix",
            nativeQuery = true)
    List<UUID> findIdsByHexPrefix(@Param("hexPrefix") String hexPrefix);

    /** Batch for the expiry job; each row is re-read under lock before it is changed. */
    @Query("select b.id from Booking b where " + LAPSED_HOLD + " order by b.temporaryHoldUntil")
    List<UUID> findLapsedHoldIds(@Param("now") Instant now, Pageable batch);
}
