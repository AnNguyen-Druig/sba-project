package com.sba.project.modules.maintenance.repository;

import com.sba.project.modules.maintenance.entity.ReportRequestService;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRequestServiceRepository extends JpaRepository<ReportRequestService, UUID> {
    List<ReportRequestService> findByReportRequestId(UUID reportRequestId);
}
