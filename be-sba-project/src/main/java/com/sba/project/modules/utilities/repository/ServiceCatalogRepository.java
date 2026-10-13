package com.sba.project.modules.utilities.repository;

import com.sba.project.modules.utilities.entity.ServiceCatalog;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for {@link ServiceCatalog} entity.
 */
@Repository
public interface ServiceCatalogRepository extends JpaRepository<ServiceCatalog, UUID>, JpaSpecificationExecutor<ServiceCatalog> {

    Optional<ServiceCatalog> findByBranchIdAndCode(UUID branchId, String code);

    List<ServiceCatalog> findByBranchIdAndActiveTrue(UUID branchId);

    boolean existsByBranchIdAndCode(UUID branchId, String code);
}
