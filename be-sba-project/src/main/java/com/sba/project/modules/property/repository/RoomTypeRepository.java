package com.sba.project.modules.property.repository;

import com.sba.project.modules.property.entity.RoomType;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomTypeRepository extends JpaRepository<RoomType, UUID> { }
