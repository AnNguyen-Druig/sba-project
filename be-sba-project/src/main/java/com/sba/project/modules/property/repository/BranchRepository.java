package com.sba.project.modules.property.repository;

import com.sba.project.modules.property.entity.Branch;
import com.sba.project.modules.property.enums.BranchStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BranchRepository extends JpaRepository<Branch, UUID> {
    List<Branch> findByManagerId(UUID managerId);
    List<Branch> findByOwnerId(UUID ownerId);
    List<Branch> findByStatus(BranchStatus status);
}
