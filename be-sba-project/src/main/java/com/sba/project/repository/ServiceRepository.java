package com.sba.project.repository;

import com.sba.project.entity.Service;
import com.sba.project.enums.ServiceStatus;
import com.sba.project.enums.ServiceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ServiceRepository extends JpaRepository<Service, UUID> {

    Page<Service> findByBranchId(UUID branchId, Pageable pageable);

    List<Service> findByBranchIdAndStatus(UUID branchId, ServiceStatus status);

    List<Service> findByBranchIdAndIsDefaultTrue(UUID branchId);

    List<Service> findByBranchIdAndServiceType(UUID branchId, ServiceType serviceType);

    boolean existsByBranchIdAndNameAndIdNot(UUID branchId, String name, UUID id);

    boolean existsByBranchIdAndName(UUID branchId, String name);

    @Query("SELECT s FROM Service s WHERE s.branchId = :branchId " +
           "AND (:status IS NULL OR s.status = :status) " +
           "AND (:serviceType IS NULL OR s.serviceType = :serviceType) " +
           "AND (:keyword IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Service> searchServices(
        @Param("branchId") UUID branchId,
        @Param("status") ServiceStatus status,
        @Param("serviceType") ServiceType serviceType,
        @Param("keyword") String keyword,
        Pageable pageable
    );
}
