package com.sba.project.mapper;

import com.sba.project.dto.request.AppointmentCreateRequest;
import com.sba.project.dto.response.AppointmentResponse;
import com.sba.project.entity.Appointment;
import com.sba.project.entity.Room;
import com.sba.project.entity.Tenant;
import org.springframework.stereotype.Component;

/**
 * Mapper for {@link Appointment} entity and its DTOs.
 */
@Component
public class AppointmentMapper {

    /**
     * Converts an {@link AppointmentCreateRequest} to a new {@link Appointment} entity.
     * The caller is responsible for:
     * <ul>
     *   <li>Resolving the {@link Tenant} from the authenticated user's identity.</li>
     *   <li>Resolving the {@link Room} by the roomId in the request.</li>
     *   <li>Setting the initial status value (e.g. "PENDING").</li>
     * </ul>
     *
     * @param request the incoming create request
     * @param tenant  resolved Tenant entity for the authenticated user
     * @param room    resolved Room entity
     * @param status  initial status value assigned by the server
     * @return a new Appointment entity (PK not yet assigned)
     */
    public Appointment toEntity(AppointmentCreateRequest request, Tenant tenant, Room room, String status) {
        return Appointment.builder()
                .tenant(tenant)
                .room(room)
                .appointmentAt(request.getAppointmentAt())
                .status(status)
                .note(request.getNote())
                .build();
    }

    /**
     * Converts an {@link Appointment} entity to an {@link AppointmentResponse}.
     *
     * @param entity the entity to convert
     * @return a response DTO
     */
    public AppointmentResponse toResponse(Appointment entity) {
        return AppointmentResponse.builder()
                .appointmentId(entity.getAppointmentId())
                .tenantId(entity.getTenant().getTenantId())
                .roomId(entity.getRoom().getRoomId())
                .appointmentAt(entity.getAppointmentAt())
                .status(entity.getStatus())
                .note(entity.getNote())
                .build();
    }
}
