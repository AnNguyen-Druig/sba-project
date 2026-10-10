package com.sba.project;

import com.sba.project.entity.Appointment;
import com.sba.project.entity.Branch;
import com.sba.project.entity.Manager;
import com.sba.project.entity.Room;
import com.sba.project.entity.RoomSlot;
import com.sba.project.entity.RoomType;
import com.sba.project.entity.Tenant;
import com.sba.project.repository.AppointmentRepository;
import com.sba.project.repository.RoomRepository;
import com.sba.project.repository.RoomSlotRepository;
import com.sba.project.repository.RoomTypeRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(properties = "spring.config.import=")
@ActiveProfiles("test")
class SoftDeleteRepositoryTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private RoomSlotRepository roomSlotRepository;

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Test
    void deletedRecordsAreHiddenAndDoNotCauseAppointmentConflicts() {
        Branch branch = Branch.builder().branchName("Central").build();
        Manager manager = Manager.builder().fullName("Manager").build();
        RoomType roomType = RoomType.builder().typeName("Studio").defaultCapacity(2).build();
        Tenant tenant = Tenant.builder().fullName("Tenant").build();
        entityManager.persist(branch);
        entityManager.persist(manager);
        entityManager.persist(roomType);
        entityManager.persist(tenant);
        entityManager.flush();

        Room room = Room.builder().branch(branch).manager(manager).roomType(roomType).roomCode("A-01")
                .referencePrice(new BigDecimal("100.00")).capacity(2).status("AVAILABLE").build();
        entityManager.persist(room);
        RoomSlot slot = RoomSlot.builder().room(room).slotCode("B1").slotName("Bed 1")
                .status("AVAILABLE").build();
        entityManager.persist(slot);
        LocalDateTime appointmentAt = LocalDateTime.of(2026, 10, 10, 12, 30);
        Appointment appointment = Appointment.builder().room(room).tenant(tenant).appointmentAt(appointmentAt)
                .status("PENDING").build();
        entityManager.persist(appointment);
        entityManager.flush();

        assertTrue(roomRepository.findById(room.getRoomId()).isPresent());
        assertEquals(1, roomRepository.findAll().size());
        assertEquals(1, roomRepository.search(null, null, null, null, null, null, null,
                PageRequest.of(0, 10)).getTotalElements());
        assertEquals(1, roomSlotRepository.findByRoom_RoomId(room.getRoomId()).size());
        assertTrue(roomTypeRepository.findById(roomType.getRoomTypeId()).isPresent());
        assertEquals(1, roomTypeRepository.findAll().size());
        assertTrue(appointmentRepository.findById(appointment.getAppointmentId()).isPresent());
        assertEquals(1, appointmentRepository.findAll().size());
        assertTrue(appointmentRepository.existsByRoom_RoomIdAndAppointmentAtAndStatusNotIn(
                room.getRoomId(), appointmentAt, java.util.Set.of("CANCELLED", "REJECTED")));

        room.setDeleted(true);
        slot.setDeleted(true);
        roomType.setDeleted(true);
        appointment.setDeleted(true);
        entityManager.flush();
        entityManager.clear();

        assertTrue(roomRepository.findById(room.getRoomId()).isEmpty());
        assertTrue(roomRepository.findAll().isEmpty());
        assertTrue(roomRepository.search(null, null, null, null, null, null, null,
                PageRequest.of(0, 10)).isEmpty());
        assertTrue(roomSlotRepository.findByRoom_RoomId(room.getRoomId()).isEmpty());
        assertTrue(roomSlotRepository.findById(slot.getRoomSlotId()).isEmpty());
        assertTrue(roomTypeRepository.findById(roomType.getRoomTypeId()).isEmpty());
        assertTrue(roomTypeRepository.findAll().isEmpty());
        assertTrue(appointmentRepository.findById(appointment.getAppointmentId()).isEmpty());
        assertTrue(appointmentRepository.findAll().isEmpty());
        assertFalse(appointmentRepository.existsByRoom_RoomIdAndAppointmentAtAndStatusNotIn(
                room.getRoomId(), appointmentAt, java.util.Set.of("CANCELLED", "REJECTED")));
        assertTrue(roomRepository.existsReferencedByAnyRoom(roomType.getRoomTypeId()));
    }
}
