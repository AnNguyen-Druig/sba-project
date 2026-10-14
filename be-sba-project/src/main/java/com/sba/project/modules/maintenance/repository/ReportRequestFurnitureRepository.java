package com.sba.project.modules.maintenance.repository;

import com.sba.project.modules.maintenance.entity.ReportRequestFurniture;
import com.sba.project.modules.maintenance.enums.ReportRequestStatus;
import java.util.Collection;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReportRequestFurnitureRepository extends JpaRepository<ReportRequestFurniture, UUID> {
    java.util.List<ReportRequestFurniture> findByReportRequestId(UUID reportRequestId);

    /** P2-29: furniture of the room reported in at least {@code minRequests} repair requests. */
    @Query("""
            select new com.sba.project.modules.maintenance.dto.response.FurnitureFailureResponse(
                l.furnitureId, count(l))
            from ReportRequestFurniture l
            where l.roomId = :roomId
            group by l.furnitureId having count(l) >= :minRequests
            order by count(l) desc, l.furnitureId
            """)
    java.util.List<com.sba.project.modules.maintenance.dto.response.FurnitureFailureResponse> findFrequentlyFailing(
            @Param("roomId") UUID roomId, @Param("minRequests") long minRequests);

    @Query("""
            select count(link) > 0 from ReportRequestFurniture link, ReportRequest request
            where request.id = link.reportRequestId
              and link.furnitureId = :furnitureId
              and request.status in :statuses
            """)
    boolean existsByFurnitureIdAndRequestStatusIn(@Param("furnitureId") UUID furnitureId,
            @Param("statuses") Collection<ReportRequestStatus> statuses);
}
