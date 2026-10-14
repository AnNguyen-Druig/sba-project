package com.sba.project.modules.maintenance.repository;

import com.sba.project.modules.maintenance.entity.FurnitureWarranty;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FurnitureWarrantyRepository extends JpaRepository<FurnitureWarranty, UUID> {
    List<FurnitureWarranty> findByFurnitureIdOrderByStartDateDesc(UUID furnitureId);

    Optional<FurnitureWarranty> findByIdAndFurnitureId(UUID id, UUID furnitureId);
}
