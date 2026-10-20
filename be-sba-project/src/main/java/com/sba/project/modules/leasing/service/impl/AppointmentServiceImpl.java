package com.sba.project.modules.leasing.service.impl;

import com.sba.project.common.exceptions.AppException;
import com.sba.project.common.exceptions.ErrorCode;
import com.sba.project.modules.leasing.dto.request.AppointmentStatusRequest;
import com.sba.project.modules.leasing.dto.response.AppointmentResponse;
import com.sba.project.modules.leasing.entity.Appointment;
import com.sba.project.modules.leasing.enums.AppointmentStatus;
import com.sba.project.modules.leasing.repository.AppointmentRepository;
import com.sba.project.modules.leasing.service.AppointmentService;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {
    private static final EnumSet<AppointmentStatus> NON_CONFLICTING = EnumSet.of(AppointmentStatus.CANCELLED, AppointmentStatus.REJECTED);
    private final AppointmentRepository appointments;

    @Override @Transactional
    public AppointmentResponse changeStatus(UUID id, AppointmentStatusRequest request) {
        Appointment appointment = requireAppointment(id);
        AppointmentStatus next = request.getStatus();
        if (!allowed(appointment.getStatus(), next))
            throw new AppException(ErrorCode.LEASING_STATE_CONFLICT, "Không thể chuyển trạng thái lịch hẹn từ " + appointment.getStatus() + " sang " + next);
        appointment.setStatus(next);
        return toResponse(appointments.save(appointment));
    }

    @Override @Transactional
    public void delete(UUID id) { requireAppointment(id).setStatus(AppointmentStatus.CANCELLED); }

    @Override @Transactional(readOnly = true)
    public void assertNoConflict(UUID roomId, LocalDateTime appointmentAt) {
        var instant = appointmentAt.atZone(ZoneId.systemDefault()).toInstant();
        if (appointments.existsByRoomIdAndStartAtAndStatusNotIn(roomId, instant, NON_CONFLICTING))
            throw new AppException(ErrorCode.DATA_CONFLICT, "Đã có lịch hẹn cho phòng tại thời điểm này");
    }

    private Appointment requireAppointment(UUID id) {
        return appointments.findById(id).orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));
    }
    private boolean allowed(AppointmentStatus current, AppointmentStatus next) {
        if (current == AppointmentStatus.PENDING)
            return next == AppointmentStatus.CONFIRMED || next == AppointmentStatus.REJECTED || next == AppointmentStatus.CANCELLED;
        return current == AppointmentStatus.CONFIRMED && (next == AppointmentStatus.COMPLETED || next == AppointmentStatus.CANCELLED);
    }
    private AppointmentResponse toResponse(Appointment a) {
        return AppointmentResponse.builder().id(a.getId()).roomId(a.getRoomId()).requesterUserId(a.getRequesterUserId())
                .assignedTo(a.getAssignedTo()).startAt(a.getStartAt()).endAt(a.getEndAt()).status(a.getStatus())
                .note(a.getNote()).createdAt(a.getCreatedAt()).updatedAt(a.getUpdatedAt()).version(a.getVersion()).build();
    }
}
