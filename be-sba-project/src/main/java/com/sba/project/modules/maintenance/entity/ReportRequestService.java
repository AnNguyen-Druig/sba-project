package com.sba.project.modules.maintenance.entity;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "report_requests_services", schema = "rental")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ReportRequestService {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "report_request_id", nullable = false)
    private UUID reportRequestId;

    @Column(name = "room_id", nullable = false)
    private UUID roomId;

    @Column(name = "room_service_id", nullable = false)
    private UUID roomServiceId;

    @Column(name = "issue_description", nullable = true, columnDefinition = "text")
    private String issueDescription;
}
