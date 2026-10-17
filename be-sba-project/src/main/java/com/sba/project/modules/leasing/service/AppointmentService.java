package com.sba.project.modules.leasing.service;

import com.sba.project.modules.leasing.dto.request.AppointmentStatusRequest;
import com.sba.project.modules.leasing.dto.response.AppointmentResponse;
import java.time.LocalDateTime;
import java.util.UUID;

public interface AppointmentService {
    AppointmentResponse changeStatus(UUID appointmentId, AppointmentStatusRequest request);
    void delete(UUID appointmentId);
    void assertNoConflict(UUID roomId, LocalDateTime appointmentAt);
}
