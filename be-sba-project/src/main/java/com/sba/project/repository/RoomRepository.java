package com.sba.project.repository;

import com.sba.project.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.UUID;

@Repository
public interface RoomRepository extends JpaRepository<Room, UUID> {

    /**
     * Kiểm tra sự tồn tại của phòng với cùng room_code trong một Branch.
     * Dùng để thực thi ràng buộc UNIQUE(branch_id, room_code) ở tầng service (P1-23).
     *
     * @param branchId ID của branch
     * @param roomCode mã phòng cần kiểm tra
     * @return {@code true} nếu đã tồn tại
     */
    boolean existsByBranch_BranchIdAndRoomCode(UUID branchId, String roomCode);

    /**
     * Danh sách phòng thuộc các Branch mà Owner/Manager được phép quản lý (P1-22, P1-29).
     *
     * @param branchIds danh sách ID branch được phép
     * @param pageable  thông tin phân trang/sắp xếp
     * @return trang kết quả
     */
    Page<Room> findByBranch_BranchIdIn(Collection<UUID> branchIds, Pageable pageable);

    /**
     * Tìm kiếm phòng theo nhiều tiêu chí lọc, hỗ trợ phân trang (P1-29, P1-38, P1-40).
     * Tất cả tham số đều nullable — khi null, tiêu chí tương ứng bị bỏ qua.
     *
     * @param branchId    lọc theo Branch (nullable)
     * @param roomTypeId  lọc theo loại phòng (nullable)
     * @param minPrice    giá tham chiếu tối thiểu (nullable)
     * @param maxPrice    giá tham chiếu tối đa (nullable)
     * @param minCapacity sức chứa tối thiểu (nullable)
     * @param status      trạng thái phòng (nullable)
     * @param address     tìm kiếm theo địa chỉ branch (LIKE, không phân biệt hoa/thường, nullable)
     * @param pageable    thông tin phân trang/sắp xếp
     * @return trang kết quả
     */
    @Query("""
            select r from Room r
            where (:branchId is null or r.branch.branchId = :branchId)
              and (:roomTypeId is null or r.roomType.roomTypeId = :roomTypeId)
              and (:minPrice is null or r.referencePrice >= :minPrice)
              and (:maxPrice is null or r.referencePrice <= :maxPrice)
              and (:minCapacity is null or r.capacity >= :minCapacity)
              and (:status is null or r.status = :status)
              and (:address is null or lower(r.branch.address) like lower(concat('%', :address, '%')))
            """)
    Page<Room> search(@Param("branchId") UUID branchId,
                      @Param("roomTypeId") UUID roomTypeId,
                      @Param("minPrice") BigDecimal minPrice,
                      @Param("maxPrice") BigDecimal maxPrice,
                      @Param("minCapacity") Integer minCapacity,
                      @Param("status") String status,
                      @Param("address") String address,
                      Pageable pageable);
}
