package com.sba.project.repository;

import com.sba.project.entity.RoomService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoomServiceRepository extends JpaRepository<RoomService, UUID> {

    List<RoomService> findByRoomIdAndActiveTrue(UUID roomId);

    List<RoomService> findByRoomId(UUID roomId);

    Optional<RoomService> findByRoomIdAndServiceId(UUID roomId, UUID serviceId);

    boolean existsByRoomIdAndServiceId(UUID roomId, UUID serviceId);
}
