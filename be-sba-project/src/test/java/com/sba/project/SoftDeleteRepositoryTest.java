package com.sba.project;

import com.sba.project.common.exceptions.AppException;
import com.sba.project.common.exceptions.ErrorCode;
import com.sba.project.modules.leasing.entity.Appointment;
import com.sba.project.modules.leasing.enums.AppointmentStatus;
import com.sba.project.modules.leasing.repository.AppointmentRepository;
import com.sba.project.modules.leasing.service.impl.AppointmentServiceImpl;
import com.sba.project.modules.property.entity.Room;
import com.sba.project.modules.property.entity.RoomSlot;
import com.sba.project.modules.property.entity.RoomType;
import com.sba.project.modules.property.enums.RoomOperationalStatus;
import com.sba.project.modules.property.enums.RoomSlotStatus;
import com.sba.project.modules.property.mapper.RoomMapper;
import com.sba.project.modules.property.repository.RoomRepository;
import com.sba.project.modules.property.repository.RoomSlotRepository;
import com.sba.project.modules.property.repository.RoomTypeRepository;
import com.sba.project.modules.property.service.impl.RoomServiceImpl;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Logical removal now changes module statuses; these service tests use mocked repositories. */
@ExtendWith(MockitoExtension.class)
class SoftDeleteRepositoryTest {
    @Mock private RoomRepository roomRepository;
    @Mock private RoomSlotRepository roomSlotRepository;
    @Mock private RoomTypeRepository roomTypeRepository;
    @Mock private AppointmentRepository appointmentRepository;
    @Mock private EntityManager entityManager;

    private RoomServiceImpl roomService;
    private AppointmentServiceImpl appointmentService;

    @BeforeEach
    void setUp() {
        roomService = new RoomServiceImpl(roomRepository, roomSlotRepository, roomTypeRepository,
                entityManager, new RoomMapper());
        appointmentService = new AppointmentServiceImpl(appointmentRepository);
    }

    @Test
    void deletingRoomDeactivatesItsSlotsAndPreservesHistory() {
        UUID roomId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        Room room = Room.builder().id(roomId).branchId(branchId).code("A-01")
                .operationalStatus(RoomOperationalStatus.AVAILABLE).build();
        RoomSlot available = RoomSlot.builder().id(UUID.randomUUID()).roomId(roomId)
                .status(RoomSlotStatus.AVAILABLE).build();
        RoomSlot maintenance = RoomSlot.builder().id(UUID.randomUUID()).roomId(roomId)
                .status(RoomSlotStatus.MAINTENANCE).build();
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(roomSlotRepository.findByRoomId(roomId)).thenReturn(List.of(available, maintenance));

        roomService.delete(roomId);

        assertEquals(RoomOperationalStatus.INACTIVE, room.getOperationalStatus());
        assertEquals(RoomSlotStatus.INACTIVE, available.getStatus());
        assertEquals(RoomSlotStatus.INACTIVE, maintenance.getStatus());
        assertEquals(roomId, room.getId());
        assertEquals(branchId, room.getBranchId());
        assertEquals("A-01", room.getCode());
        assertEquals(roomId, available.getRoomId());
        verify(roomRepository, never()).delete(any(Room.class));
        verify(roomRepository, never()).deleteById(any(UUID.class));
        verify(roomSlotRepository, never()).delete(any(RoomSlot.class));
        verify(roomSlotRepository, never()).deleteById(any(UUID.class));
    }

    @Test
    void deletingSlotDeactivatesItWithoutRemovingItsRoomLink() {
        UUID slotId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        RoomSlot slot = RoomSlot.builder().id(slotId).roomId(roomId).code("B1")
                .status(RoomSlotStatus.AVAILABLE).build();
        when(roomSlotRepository.findById(slotId)).thenReturn(Optional.of(slot));

        roomService.deleteRoomSlot(slotId);

        assertEquals(RoomSlotStatus.INACTIVE, slot.getStatus());
        assertEquals(slotId, slot.getId());
        assertEquals(roomId, slot.getRoomId());
        assertEquals("B1", slot.getCode());
        verify(roomSlotRepository, never()).delete(any(RoomSlot.class));
        verify(roomSlotRepository, never()).deleteById(any(UUID.class));
    }

    @Test
    void deletingUnusedRoomTypeMarksItInactive() {
        UUID typeId = UUID.randomUUID();
        RoomType type = RoomType.builder().id(typeId).name("Studio").active(true).build();
        when(roomTypeRepository.findById(typeId)).thenReturn(Optional.of(type));
        when(roomRepository.existsByRoomTypeId(typeId)).thenReturn(false);

        roomService.deleteRoomType(typeId);

        assertFalse(type.getActive());
        assertEquals(typeId, type.getId());
        assertEquals("Studio", type.getName());
        verify(roomTypeRepository, never()).delete(any(RoomType.class));
        verify(roomTypeRepository, never()).deleteById(any(UUID.class));
    }

    @Test
    void deletingReferencedRoomTypeIsRejectedWithoutChangingItsStatus() {
        UUID typeId = UUID.randomUUID();
        RoomType type = RoomType.builder().id(typeId).active(true).build();
        when(roomTypeRepository.findById(typeId)).thenReturn(Optional.of(type));
        when(roomRepository.existsByRoomTypeId(typeId)).thenReturn(true);

        AppException error = assertThrows(AppException.class, () -> roomService.deleteRoomType(typeId));

        assertEquals(ErrorCode.DATA_CONFLICT, error.getErrorCode());
        assertTrue(type.getActive());
        verify(roomTypeRepository, never()).delete(any(RoomType.class));
        verify(roomTypeRepository, never()).deleteById(any(UUID.class));
    }

    @Test
    void deletingAppointmentCancelsItWithoutRemovingItsHistory() {
        UUID appointmentId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        Instant startAt = Instant.parse("2026-10-10T05:30:00Z");
        Appointment appointment = Appointment.builder().id(appointmentId).roomId(roomId)
                .requesterUserId(requesterId).startAt(startAt).endAt(startAt.plusSeconds(1800))
                .status(AppointmentStatus.PENDING).build();
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));

        appointmentService.delete(appointmentId);

        assertEquals(AppointmentStatus.CANCELLED, appointment.getStatus());
        assertEquals(appointmentId, appointment.getId());
        assertEquals(roomId, appointment.getRoomId());
        assertEquals(requesterId, appointment.getRequesterUserId());
        assertEquals(startAt, appointment.getStartAt());
        assertEquals(startAt.plusSeconds(1800), appointment.getEndAt());
        verify(appointmentRepository, never()).delete(any(Appointment.class));
        verify(appointmentRepository, never()).deleteById(any(UUID.class));
    }

    @Test
    void conflictCheckExcludesCancelledAndRejectedAppointments() {
        UUID roomId = UUID.randomUUID();
        LocalDateTime localTime = LocalDateTime.of(2026, 10, 10, 12, 30);
        Instant startAt = localTime.atZone(ZoneId.systemDefault()).toInstant();
        EnumSet<AppointmentStatus> excluded = EnumSet.of(AppointmentStatus.CANCELLED, AppointmentStatus.REJECTED);
        when(appointmentRepository.existsByRoomIdAndStartAtAndStatusNotIn(roomId, startAt, excluded))
                .thenReturn(false);

        assertDoesNotThrow(() -> appointmentService.assertNoConflict(roomId, localTime));

        verify(appointmentRepository).existsByRoomIdAndStartAtAndStatusNotIn(roomId, startAt, excluded);
    }

    @Test
    void activeAppointmentStillBlocksTheSameTimeSlot() {
        UUID roomId = UUID.randomUUID();
        LocalDateTime localTime = LocalDateTime.of(2026, 10, 10, 12, 30);
        Instant startAt = localTime.atZone(ZoneId.systemDefault()).toInstant();
        EnumSet<AppointmentStatus> excluded = EnumSet.of(AppointmentStatus.CANCELLED, AppointmentStatus.REJECTED);
        when(appointmentRepository.existsByRoomIdAndStartAtAndStatusNotIn(roomId, startAt, excluded))
                .thenReturn(true);

        AppException error = assertThrows(AppException.class,
                () -> appointmentService.assertNoConflict(roomId, localTime));

        assertEquals(ErrorCode.DATA_CONFLICT, error.getErrorCode());
    }
}
