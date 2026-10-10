package com.sba.project.repository;

import com.sba.project.entity.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    /**
     * Danh sách lịch hẹn của một Tenant (người thuê xem lịch của mình) (P1-45).
     *
     * @param tenantId ID của tenant
     * @param pageable thông tin phân trang/sắp xếp
     * @return trang kết quả
     */
    Page<Appointment> findByTenant_TenantId(UUID tenantId, Pageable pageable);

    /**
     * Danh sách lịch hẹn trong các Branch mà Owner/Manager được phép quản lý (P1-44).
     *
     * @param branchIds danh sách ID branch được phép
     * @param pageable  thông tin phân trang/sắp xếp
     * @return trang kết quả
     */
    Page<Appointment> findByRoom_Branch_BranchIdIn(Collection<UUID> branchIds, Pageable pageable);

    /**
     * Kiểm tra sự tồn tại của lịch hẹn trùng thời điểm trên cùng một phòng,
     * loại trừ các lịch có status nằm trong {@code excludedStatuses} (P1-46).
     * <p>
     * Ví dụ: {@code excludedStatuses = ["CANCELLED", "REJECTED"]} để bỏ qua lịch đã hủy.
     *
     * @param roomId           ID phòng cần kiểm tra
     * @param appointmentAt    thời điểm hẹn
     * @param excludedStatuses danh sách status bị loại trừ (lịch hủy/từ chối không gây trùng)
     * @return {@code true} nếu đã có lịch hẹn hợp lệ trùng thời điểm
     */
    boolean existsByRoom_RoomIdAndAppointmentAtAndStatusNotIn(UUID roomId,
                                                               LocalDateTime appointmentAt,
                                                               Collection<String> excludedStatuses);
}
