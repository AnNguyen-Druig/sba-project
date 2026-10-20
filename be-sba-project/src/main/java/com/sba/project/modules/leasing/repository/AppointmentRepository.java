package com.sba.project.modules.leasing.repository;

import com.sba.project.modules.leasing.entity.Appointment;
import java.time.Instant;
import java.util.Collection;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
    boolean existsByRoomIdAndStartAtAndStatusNotIn(UUID roomId, Instant startAt, Collection<com.sba.project.modules.leasing.enums.AppointmentStatus> excludedStatuses);
}
