package com.enterprise.incident.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardKpiDto {
    private long openIncidents;
    private long criticalIncidents;
    private long slaBreaches;
    private double resolutionRate; // percentage (e.g. 85.5)
    private double mttrHours;      // Mean Time To Resolution
    private double mttaHours;      // Mean Time To Acknowledge
    private Map<String, Long> priorityDistribution;
    private Map<String, Long> statusDistribution;
}
