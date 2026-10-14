package com.sba.project.modules.leasing.repository;

import com.sba.project.modules.leasing.entity.RoomSlotOccupancy;
import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.Repository;

/** READ-ONLY occupancy history (P1-34 history). Occupancy is written in Student 3's lease conversion. */
public interface OccupancyReadRepository extends Repository<RoomSlotOccupancy, UUID> {
    List<RoomSlotOccupancy> findByContractTenantIdOrderByStartDateAsc(UUID contractTenantId);
}
