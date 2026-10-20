package com.sba.project.modules.property.repository;

import com.sba.project.modules.property.entity.RoomSlot;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomSlotRepository extends JpaRepository<RoomSlot, UUID> {
    List<RoomSlot> findByRoomId(UUID roomId);
}
