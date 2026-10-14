package com.sba.project.modules.maintenance.repository;

import com.sba.project.modules.maintenance.entity.ReportRequestFurniture;
import com.sba.project.modules.maintenance.enums.ReportRequestStatus;
import java.util.Collection;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReportRequestFurnitureRepository extends JpaRepository<ReportRequestFurniture, UUID> {
    @Query("""
            select count(link) > 0 from ReportRequestFurniture link, ReportRequest request
            where request.id = link.reportRequestId
              and link.furnitureId = :furnitureId
              and request.status in :statuses
            """)
    boolean existsByFurnitureIdAndRequestStatusIn(@Param("furnitureId") UUID furnitureId,
            @Param("statuses") Collection<ReportRequestStatus> statuses);
}
