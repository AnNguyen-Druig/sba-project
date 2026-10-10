package com.sba.project.repository;

import com.sba.project.entity.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RoomTypeRepository extends JpaRepository<RoomType, UUID> {
    // Không có custom method: task chỉ cần CRUD cơ bản (P1-21)
}
