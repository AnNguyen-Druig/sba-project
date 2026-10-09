package com.sba.project.repository;

import com.sba.project.entity.RoomServiceCharge;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface RoomServiceChargeRepository extends JpaRepository<RoomServiceCharge, UUID> {

    List<RoomServiceCharge> findByRoomIdAndBillingPeriodStartAndBillingPeriodEnd(
        UUID roomId, LocalDate start, LocalDate end
    );

    Page<RoomServiceCharge> findByRoomId(UUID roomId, Pageable pageable);

    boolean existsByRoomServiceIdAndBillingPeriodStartAndBillingPeriodEnd(
        UUID roomServiceId, LocalDate start, LocalDate end
    );

    @Query("SELECT rsc FROM RoomServiceCharge rsc " +
           "JOIN rsc.roomService rs " +
           "JOIN rs.service s " +
           "WHERE rsc.roomId = :roomId " +
           "AND rsc.billingPeriodStart >= :from AND rsc.billingPeriodEnd <= :to")
    List<RoomServiceCharge> findByRoomIdAndPeriodRange(
        @Param("roomId") UUID roomId,
        @Param("from") LocalDate from,
        @Param("to") LocalDate to
    );

    List<RoomServiceCharge> findByRoomIdAndFinalizedFalse(UUID roomId);
}
