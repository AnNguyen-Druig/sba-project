package com.sba.project.modules.property.repository;

import com.sba.project.modules.property.entity.Branch;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BranchRepository extends JpaRepository<Branch, UUID> {
}
