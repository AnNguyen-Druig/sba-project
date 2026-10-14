package com.sba.project.modules.maintenance.config;

import com.sba.project.modules.maintenance.enums.ReportRequestPriority;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * P2-30: repair deadlines per priority are system configuration (no schema column stores them), e.g.
 * {@code app.maintenance.sla-hours.URGENT=24}. The project has not fixed any value, so by default no
 * priority has a deadline and requests get no sla_due_at.
 */
@Component
@ConfigurationProperties(prefix = "app.maintenance")
public class MaintenanceProperties {
    private Map<ReportRequestPriority, Integer> slaHours = new EnumMap<>(ReportRequestPriority.class);

    public Map<ReportRequestPriority, Integer> getSlaHours() {
        return slaHours;
    }

    public void setSlaHours(Map<ReportRequestPriority, Integer> slaHours) {
        this.slaHours = slaHours == null ? new EnumMap<>(ReportRequestPriority.class) : slaHours;
    }

    /** Configured deadline in hours for the priority, or 0 when none is configured. */
    public int slaHoursFor(ReportRequestPriority priority) {
        Integer hours = slaHours.get(priority);
        return hours == null ? 0 : Math.max(hours, 0);
    }
}
