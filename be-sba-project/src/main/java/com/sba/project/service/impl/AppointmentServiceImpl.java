package com.sba.project.service.impl;

import com.sba.project.dto.request.AppointmentStatusRequest;
import com.sba.project.dto.response.AppointmentResponse;
import com.sba.project.entity.Appointment;
import com.sba.project.exception.BusinessException;
import com.sba.project.exception.DuplicateResourceException;
import com.sba.project.exception.ResourceNotFoundException;
import com.sba.project.mapper.AppointmentMapper;
import com.sba.project.repository.AppointmentRepository;
import com.sba.project.service.AppointmentService;
import com.sba.project.service.AppointmentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private static final Set<String> VALID_STATUSES = Set.of(
            AppointmentStatus.PENDING,
            AppointmentStatus.CONFIRMED,
            AppointmentStatus.COMPLETED,
            AppointmentStatus.CANCELLED,
            AppointmentStatus.REJECTED);
    private static final Set<String> NON_CONFLICTING_STATUSES = Set.of(
            AppointmentStatus.CANCELLED, AppointmentStatus.REJECTED);

    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;

    @Override
    @Transactional
    public AppointmentResponse changeStatus(UUID appointmentId, AppointmentStatusRequest request) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch hẹn: " + appointmentId));
        if (!VALID_STATUSES.contains(request.getStatus())) {
            throw new BusinessException("Trạng thái lịch hẹn không hợp lệ: " + request.getStatus());
        }
        if (!isAllowedTransition(appointment.getStatus(), request.getStatus())) {
            throw new DuplicateResourceException("Không thể chuyển trạng thái lịch hẹn từ "
                    + appointment.getStatus() + " sang " + request.getStatus());
        }

        appointment.setStatus(request.getStatus());
        return appointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    @Override
    @Transactional(readOnly = true)
    public void assertNoConflict(UUID roomId, LocalDateTime appointmentAt) {
        boolean conflict = appointmentRepository.existsByRoom_RoomIdAndAppointmentAtAndStatusNotIn(
                roomId, appointmentAt, NON_CONFLICTING_STATUSES);
        if (conflict) {
            throw new DuplicateResourceException("Đã có lịch hẹn cho phòng tại thời điểm này");
        }
    }

    private boolean isAllowedTransition(String currentStatus, String nextStatus) {
        if (AppointmentStatus.PENDING.equals(currentStatus)) {
            return Set.of(AppointmentStatus.CONFIRMED, AppointmentStatus.REJECTED, AppointmentStatus.CANCELLED)
                    .contains(nextStatus);
        }
        return AppointmentStatus.CONFIRMED.equals(currentStatus)
                && Set.of(AppointmentStatus.COMPLETED, AppointmentStatus.CANCELLED).contains(nextStatus);
    }
}
