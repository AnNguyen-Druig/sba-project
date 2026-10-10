package com.sba.project.repository;

import com.sba.project.entity.RoomSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RoomSlotRepository extends JpaRepository<RoomSlot, UUID> {

    /**
     * Lấy danh sách tất cả slot của một phòng (P1-25, P1-26).
     *
     * @param roomId ID của phòng
     * @return danh sách slot
     */
    List<RoomSlot> findByRoom_RoomId(UUID roomId);
}
