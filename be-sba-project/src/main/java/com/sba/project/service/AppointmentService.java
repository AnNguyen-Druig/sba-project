package com.sba.project.service;

import com.sba.project.dto.request.AppointmentStatusRequest;
import com.sba.project.dto.response.AppointmentResponse;

import java.time.LocalDateTime;
import java.util.UUID;

public interface AppointmentService {

    AppointmentResponse changeStatus(UUID appointmentId, AppointmentStatusRequest request);

    void assertNoConflict(UUID roomId, LocalDateTime appointmentAt);
}
