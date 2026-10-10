package com.sba.project.mapper;

import com.sba.project.dto.request.RoomRequest;
import com.sba.project.dto.request.RoomSlotRequest;
import com.sba.project.dto.request.RoomTypeRequest;
import com.sba.project.dto.response.AppointmentResponse;
import com.sba.project.dto.response.PublicRoomResponse;
import com.sba.project.entity.Appointment;
import com.sba.project.entity.Branch;
import com.sba.project.entity.Manager;
import com.sba.project.entity.Room;
import com.sba.project.entity.RoomSlot;
import com.sba.project.entity.RoomType;
import com.sba.project.entity.Tenant;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MapperTest {

    @Test
    void roomTypeMapperMapsAndUpdatesAllFields() {
        RoomTypeMapper mapper = new RoomTypeMapper();
        RoomTypeRequest request = RoomTypeRequest.builder()
                .typeName("Studio")
                .description("Compact")
                .defaultCapacity(2)
                .features("Balcony")
                .build();

        RoomType entity = mapper.toEntity(request);
        assertNull(entity.getRoomTypeId());
        assertEquals("Studio", entity.getTypeName());
        assertEquals("Compact", entity.getDescription());
        assertEquals(2, entity.getDefaultCapacity());
        assertEquals("Balcony", mapper.toResponse(entity).getFeatures());
        assertEquals(entity.getTypeName(), mapper.toResponse(entity).getTypeName());

        mapper.updateEntity(entity, RoomTypeRequest.builder().typeName("Suite").defaultCapacity(3).build());
        assertEquals("Suite", entity.getTypeName());
        assertEquals(3, entity.getDefaultCapacity());
    }

    @Test
    void roomMapperProducesExpectedPublicFields() {
        RoomMapper mapper = new RoomMapper();
        Branch branch = Branch.builder().branchId(UUID.randomUUID()).branchName("Central").address("Main St").build();
        Manager manager = Manager.builder().managerId(UUID.randomUUID()).build();
        RoomType type = RoomType.builder().roomTypeId(UUID.randomUUID()).typeName("Studio").build();
        RoomRequest request = RoomRequest.builder()
                .roomCode("A-01").referencePrice(new BigDecimal("100.00")).capacity(2)
                .status("AVAILABLE").description("Sunny").build();

        Room room = mapper.toEntity(request, branch, manager, type);
        assertNull(room.getRoomId());
        PublicRoomResponse expected = PublicRoomResponse.builder()
                .branchId(branch.getBranchId()).branchName("Central").address("Main St")
                .roomTypeId(type.getRoomTypeId()).typeName("Studio").roomCode("A-01")
                .referencePrice(new BigDecimal("100.00")).capacity(2).status("AVAILABLE")
                .description("Sunny").build();
        assertEquals(expected, mapper.toPublicResponse(room));

        mapper.updateEntity(room, request, branch, manager, type);
        assertEquals(room.getRoomId(), mapper.toResponse(room).getRoomId());
        assertEquals(manager.getManagerId(), mapper.toResponse(room).getManagerId());
    }

    @Test
    void roomSlotMapperMapsRequestAndResponse() {
        RoomSlotMapper mapper = new RoomSlotMapper();
        Room room = Room.builder().roomId(UUID.randomUUID()).build();
        RoomSlot slot = mapper.toEntity(RoomSlotRequest.builder().roomId(room.getRoomId())
                .slotCode("B1").slotName("Bed 1").status("AVAILABLE").build(), room);

        assertNull(slot.getRoomSlotId());
        assertEquals(room.getRoomId(), mapper.toResponse(slot).getRoomId());
        assertEquals("B1", mapper.toResponse(slot).getSlotCode());

        mapper.updateEntity(slot, RoomSlotRequest.builder().roomId(room.getRoomId())
                .slotCode("B2").slotName("Bed 2").status("MAINTENANCE").description("Window side").build(), room);
        assertEquals("B2", mapper.toResponse(slot).getSlotCode());
        assertEquals("MAINTENANCE", mapper.toResponse(slot).getStatus());
    }

    @Test
    void appointmentMapperMapsResponseWithoutChangingEntity() {
        AppointmentMapper mapper = new AppointmentMapper();
        UUID tenantId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        LocalDateTime appointmentAt = LocalDateTime.of(2026, 10, 10, 12, 30);
        Appointment appointment = Appointment.builder()
                .appointmentId(UUID.randomUUID())
                .tenant(Tenant.builder().tenantId(tenantId).build())
                .room(Room.builder().roomId(roomId).build())
                .appointmentAt(appointmentAt).status("PENDING").note("Visit").build();

        assertEquals(AppointmentResponse.builder().appointmentId(appointment.getAppointmentId())
                .tenantId(tenantId).roomId(roomId).appointmentAt(appointmentAt)
                .status("PENDING").note("Visit").build(), mapper.toResponse(appointment));
    }
}
