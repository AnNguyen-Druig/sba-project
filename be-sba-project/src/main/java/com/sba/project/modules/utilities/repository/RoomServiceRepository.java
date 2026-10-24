package com.sba.project.modules.utilities.repository;

import com.sba.project.modules.utilities.entity.RoomService;
import com.sba.project.modules.utilities.enums.RoomServiceStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for {@link RoomService} entity.
 */
@Repository
public interface RoomServiceRepository extends JpaRepository<RoomService, UUID>, JpaSpecificationExecutor<RoomService> {

    List<RoomService> findByRoomIdAndStatus(UUID roomId, RoomServiceStatus status);

    List<RoomService> findByBranchIdAndStatus(UUID branchId, RoomServiceStatus status);

    Optional<RoomService> findByRoomIdAndServiceId(UUID roomId, UUID serviceId);

    List<RoomService> findByRoomId(UUID roomId);

    boolean existsByServiceId(UUID serviceId);
}
