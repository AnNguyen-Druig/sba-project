package com.sba.project.modules.maintenance.repository;

import com.sba.project.modules.maintenance.entity.Furniture;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FurnitureRepository extends JpaRepository<Furniture, UUID>, JpaSpecificationExecutor<Furniture> {
    /** Mirrors UNIQUE(room_id, asset_code) so the common conflict gets a specific error code. */
    boolean existsByRoomIdAndAssetCode(UUID roomId, String assetCode);

    boolean existsByRoomIdAndAssetCodeAndIdNot(UUID roomId, String assetCode, UUID id);
}
