package com.sba.project.modules.utilities.repository;

import com.sba.project.modules.utilities.entity.RoomServiceCharge;
import com.sba.project.modules.utilities.enums.RoomServiceChargeStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for {@link RoomServiceCharge} entity.
 */
@Repository
public interface RoomServiceChargeRepository extends JpaRepository<RoomServiceCharge, UUID>, JpaSpecificationExecutor<RoomServiceCharge> {

    List<RoomServiceCharge> findByRoomServiceIdAndBillingMonth(UUID roomServiceId, LocalDate billingMonth);

    List<RoomServiceCharge> findByRoomIdAndBillingMonthAndStatus(UUID roomId, LocalDate billingMonth, RoomServiceChargeStatus status);

    boolean existsByRoomServiceIdAndBillingMonthAndStatusNot(UUID roomServiceId, LocalDate billingMonth, RoomServiceChargeStatus status);

    List<RoomServiceCharge> findByRoomIdAndBillingMonth(UUID roomId, LocalDate billingMonth);
}
