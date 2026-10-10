package com.sba.project.service;

import com.sba.project.dto.request.AppointmentStatusRequest;
import com.sba.project.dto.response.AppointmentResponse;
import com.sba.project.entity.Appointment;
import com.sba.project.entity.Room;
import com.sba.project.entity.Tenant;
import com.sba.project.exception.BusinessException;
import com.sba.project.exception.DuplicateResourceException;
import com.sba.project.exception.ResourceNotFoundException;
import com.sba.project.mapper.AppointmentMapper;
import com.sba.project.repository.AppointmentRepository;
import com.sba.project.service.impl.AppointmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AppointmentServiceTest {

    private AppointmentRepository appointmentRepository;
    private AppointmentServiceImpl service;

    @BeforeEach
    void setUp() {
        appointmentRepository = mock(AppointmentRepository.class);
        service = new AppointmentServiceImpl(appointmentRepository, new AppointmentMapper());
    }

    @Test
    void changeStatus_pendingToConfirmed_ok() {
        Appointment appointment = appointment("PENDING");
        when(appointmentRepository.findById(appointment.getAppointmentId())).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppointmentResponse response = service.changeStatus(appointment.getAppointmentId(), status("CONFIRMED"));

        assertEquals("CONFIRMED", response.getStatus());
        assertEquals("Visit", response.getNote());
        verify(appointmentRepository).save(appointment);
    }

    @Test
    void changeStatus_allAllowedTransitions_ok() {
        for (String[] transition : new String[][]{
                {"PENDING", "REJECTED"},
                {"PENDING", "CANCELLED"},
                {"CONFIRMED", "COMPLETED"},
                {"CONFIRMED", "CANCELLED"}}) {
            Appointment appointment = appointment(transition[0]);
            when(appointmentRepository.findById(appointment.getAppointmentId())).thenReturn(Optional.of(appointment));
            when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

            assertEquals(transition[1], service.changeStatus(appointment.getAppointmentId(), status(transition[1])).getStatus());
        }
    }

    @Test
    void changeStatus_pendingToCompleted_throws409() {
        Appointment appointment = appointment("PENDING");
        when(appointmentRepository.findById(appointment.getAppointmentId())).thenReturn(Optional.of(appointment));

        assertThrows(DuplicateResourceException.class,
                () -> service.changeStatus(appointment.getAppointmentId(), status("COMPLETED")));
    }

    @Test
    void changeStatus_fromTerminal_throws409() {
        Appointment appointment = appointment("CANCELLED");
        when(appointmentRepository.findById(appointment.getAppointmentId())).thenReturn(Optional.of(appointment));

        assertThrows(DuplicateResourceException.class,
                () -> service.changeStatus(appointment.getAppointmentId(), status("CONFIRMED")));
    }

    @Test
    void changeStatus_unknownStatus_throws400() {
        Appointment appointment = appointment("PENDING");
        when(appointmentRepository.findById(appointment.getAppointmentId())).thenReturn(Optional.of(appointment));

        assertThrows(BusinessException.class,
                () -> service.changeStatus(appointment.getAppointmentId(), status("FUTURE")));
    }

    @Test
    void changeStatus_notFound_throws404() {
        UUID id = UUID.randomUUID();
        when(appointmentRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.changeStatus(id, status("CONFIRMED")));
    }

    @Test
    void assertNoConflict_existing_throws409() {
        UUID roomId = UUID.randomUUID();
        LocalDateTime appointmentAt = LocalDateTime.of(2026, 10, 10, 12, 30);
        when(appointmentRepository.existsByRoom_RoomIdAndAppointmentAtAndStatusNotIn(any(), any(), any()))
                .thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> service.assertNoConflict(roomId, appointmentAt));
    }

    @Test
    void assertNoConflict_none_ok() {
        UUID roomId = UUID.randomUUID();
        LocalDateTime appointmentAt = LocalDateTime.of(2026, 10, 10, 12, 30);
        when(appointmentRepository.existsByRoom_RoomIdAndAppointmentAtAndStatusNotIn(any(), any(), any()))
                .thenReturn(false);

        service.assertNoConflict(roomId, appointmentAt);

        verify(appointmentRepository).existsByRoom_RoomIdAndAppointmentAtAndStatusNotIn(
                roomId, appointmentAt, Set.of("CANCELLED", "REJECTED"));
    }

    @Test
    void assertNoConflict_excludesCancelledAndRejected() {
        UUID roomId = UUID.randomUUID();
        LocalDateTime appointmentAt = LocalDateTime.of(2026, 10, 10, 12, 30);
        when(appointmentRepository.existsByRoom_RoomIdAndAppointmentAtAndStatusNotIn(any(), any(), any()))
                .thenReturn(false);

        service.assertNoConflict(roomId, appointmentAt);

        verify(appointmentRepository).existsByRoom_RoomIdAndAppointmentAtAndStatusNotIn(
                roomId, appointmentAt, Set.of("CANCELLED", "REJECTED"));
    }

    private AppointmentStatusRequest status(String status) {
        return AppointmentStatusRequest.builder().status(status).note("Not persisted").build();
    }

    private Appointment appointment(String status) {
        return Appointment.builder().appointmentId(UUID.randomUUID())
                .tenant(Tenant.builder().tenantId(UUID.randomUUID()).build())
                .room(Room.builder().roomId(UUID.randomUUID()).build())
                .appointmentAt(LocalDateTime.of(2026, 10, 10, 12, 30)).status(status).note("Visit").build();
    }
}
