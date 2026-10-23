package com.sba.project.modules.leasing.repository;

import com.sba.project.modules.leasing.entity.ResourceAllocation;
import com.sba.project.modules.leasing.enums.ResourceAllocationStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Periods are {@code [start_date, end_date)}, matching the schema's daterange overlap trigger. */
public interface ResourceAllocationRepository extends JpaRepository<ResourceAllocation, UUID> {
    String NOT_RELEASED = " a.status <> com.sba.project.modules.leasing.enums.ResourceAllocationStatus.RELEASED ";
    String OVERLAPS = " a.startDate < :endExclusive and a.endDate > :start ";

    /**
     * Transaction-scoped advisory lock serializing every hold/lease decision on one Room (BR-P10b).
     * Released automatically on commit/rollback.
     */
    @Query(value = "select count(*) from (select pg_advisory_xact_lock(hashtextextended(:roomId, 0))) l",
            nativeQuery = true)
    long lockRoom(@Param("roomId") String roomId);

    /** Whole-room request: any live allocation in the Room conflicts (Room-level or any Slot). */
    @Query("select count(a) > 0 from ResourceAllocation a where a.roomId = :roomId and" + NOT_RELEASED
            + "and" + OVERLAPS)
    boolean existsLiveInRoom(@Param("roomId") UUID roomId, @Param("start") LocalDate start,
            @Param("endExclusive") LocalDate endExclusive);

    /** Slot request: the same Slot or a Room-level allocation conflicts. */
    @Query("select count(a) > 0 from ResourceAllocation a where a.roomId = :roomId and" + NOT_RELEASED
            + "and (a.roomSlotId is null or a.roomSlotId = :slotId) and" + OVERLAPS)
    boolean existsLiveForSlot(@Param("roomId") UUID roomId, @Param("slotId") UUID slotId,
            @Param("start") LocalDate start, @Param("endExclusive") LocalDate endExclusive);

    List<ResourceAllocation> findByBookingIdAndStatusNot(UUID bookingId, ResourceAllocationStatus status);
}
