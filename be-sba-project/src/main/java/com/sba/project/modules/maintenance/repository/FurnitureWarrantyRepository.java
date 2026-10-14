package com.sba.project.modules.maintenance.repository;

import com.sba.project.modules.maintenance.entity.FurnitureWarranty;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FurnitureWarrantyRepository extends JpaRepository<FurnitureWarranty, UUID> {
    List<FurnitureWarranty> findByFurnitureIdOrderByStartDateDesc(UUID furnitureId);

    Optional<FurnitureWarranty> findByIdAndFurnitureId(UUID id, UUID furnitureId);

    /** P2-29: warranties of the room's furniture that end inside [from, to]. */
    @Query("""
            select w from FurnitureWarranty w, Furniture f
            where f.id = w.furnitureId and f.roomId = :roomId and w.endDate between :from and :to
            order by w.endDate, w.id
            """)
    List<FurnitureWarranty> findExpiringInRoom(@Param("roomId") UUID roomId, @Param("from") LocalDate from,
            @Param("to") LocalDate to);
}
