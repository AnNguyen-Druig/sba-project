package com.sba.project.modules.property.repository;

import com.sba.project.modules.property.entity.Room;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoomRepository extends JpaRepository<Room, UUID> {
    boolean existsByBranchIdAndCode(UUID branchId, String code);
    boolean existsByBranchIdAndCodeAndIdNot(UUID branchId, String code, UUID id);
    boolean existsByRoomTypeId(UUID roomTypeId);

    @Query("""
        select r from Room r join r.branchReference b
        where (:publicOnly = false or r.isPublic = true)
          and (:branchId is null or r.branchId = :branchId)
          and (:roomTypeId is null or r.roomTypeId = :roomTypeId)
          and (:minPrice is null or r.referencePrice >= :minPrice)
          and (:maxPrice is null or r.referencePrice <= :maxPrice)
          and (:minCapacity is null or r.capacity >= :minCapacity)
          and (cast(:status as string) is null or str(r.operationalStatus) = :status)
          and (cast(:addressPattern as string) is null or lower(b.address) like cast(:addressPattern as string))
        """)
    Page<Room> search(@Param("publicOnly") boolean publicOnly, @Param("branchId") UUID branchId,
                      @Param("roomTypeId") UUID roomTypeId, @Param("minPrice") BigDecimal minPrice,
                      @Param("maxPrice") BigDecimal maxPrice, @Param("minCapacity") Integer minCapacity,
                      @Param("status") String status, @Param("addressPattern") String addressPattern, Pageable pageable);
}
