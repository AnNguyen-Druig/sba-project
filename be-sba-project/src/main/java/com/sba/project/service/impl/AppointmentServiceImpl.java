package com.sba.project.service.impl;

import com.sba.project.dto.request.AppointmentStatusRequest;
import com.sba.project.dto.response.AppointmentResponse;
import com.sba.project.entity.Appointment;
import com.sba.project.enums.AppointmentStatus;
import com.sba.project.exception.BusinessException;
import com.sba.project.exception.DuplicateResourceException;
import com.sba.project.exception.ResourceNotFoundException;
import com.sba.project.mapper.AppointmentMapper;
import com.sba.project.repository.AppointmentRepository;
import com.sba.project.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private static final Set<String> NON_CONFLICTING_STATUSES = Set.of(
            AppointmentStatus.CANCELLED.name(), AppointmentStatus.REJECTED.name());

    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;

    @Override
    @Transactional
    public AppointmentResponse changeStatus(UUID appointmentId, AppointmentStatusRequest request) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch hẹn: " + appointmentId));
        AppointmentStatus nextStatus = parseStatus(request.getStatus());
        if (!isAllowedTransition(appointment.getStatus(), nextStatus)) {
            throw new DuplicateResourceException("Không thể chuyển trạng thái lịch hẹn từ "
                    + appointment.getStatus() + " sang " + request.getStatus());
        }

        appointment.setStatus(nextStatus.name());
        return appointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    @Override
    @Transactional
    public void delete(UUID appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch hẹn: " + appointmentId));
        appointment.setDeleted(true);
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

    private AppointmentStatus parseStatus(String status) {
        try {
            return AppointmentStatus.valueOf(status);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new BusinessException("Trạng thái lịch hẹn không hợp lệ: " + status);
        }
    }

    private boolean isAllowedTransition(String currentStatus, AppointmentStatus nextStatus) {
        if (AppointmentStatus.PENDING.name().equals(currentStatus)) {
            return nextStatus == AppointmentStatus.CONFIRMED
                    || nextStatus == AppointmentStatus.REJECTED
                    || nextStatus == AppointmentStatus.CANCELLED;
        }
        return AppointmentStatus.CONFIRMED.name().equals(currentStatus)
                && (nextStatus == AppointmentStatus.COMPLETED || nextStatus == AppointmentStatus.CANCELLED);
    }
}
