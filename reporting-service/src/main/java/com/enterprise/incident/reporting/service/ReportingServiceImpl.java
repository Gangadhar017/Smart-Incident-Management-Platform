package com.enterprise.incident.reporting.service;

import com.enterprise.incident.reporting.dto.DashboardKpiDto;
import com.enterprise.incident.reporting.entity.Incident;
import com.enterprise.incident.reporting.repository.IncidentRepository;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportingServiceImpl implements ReportingService {

    private final IncidentRepository incidentRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardKpiDto getDashboardKpis() {
        List<Incident> incidents = incidentRepository.findAll();

        long openIncidents = incidents.stream()
                .filter(i -> !List.of("RESOLVED", "CLOSED", "CANCELLED").contains(i.getStatus()))
                .count();

        long criticalIncidents = incidents.stream()
                .filter(i -> "P1".equalsIgnoreCase(i.getPriority()))
                .count();

        long slaBreaches = incidents.stream()
                .filter(Incident::isSlaBreached)
                .count();

        long resolvedOrClosed = incidents.stream()
                .filter(i -> List.of("RESOLVED", "CLOSED").contains(i.getStatus()))
                .count();

        double resolutionRate = incidents.isEmpty() ? 0.0 : ((double) resolvedOrClosed / incidents.size()) * 100.0;

        // Calculate MTTR (Mean Time to Resolution) in hours
        double totalMttrHours = 0.0;
        long mttrCount = 0;
        for (Incident i : incidents) {
            if (i.getResolvedDate() != null) {
                double diffHours = Duration.between(i.getCreatedDate(), i.getResolvedDate()).toMinutes() / 60.0;
                totalMttrHours += diffHours;
                mttrCount++;
            }
        }
        double mttr = mttrCount == 0 ? 0.0 : totalMttrHours / mttrCount;

        // Calculate MTTA (Mean Time to Acknowledge) in hours
        double totalMttaHours = 0.0;
        long mttaCount = 0;
        for (Incident i : incidents) {
            if (i.getAssigneeId() != null) {
                // Mock acknowledgement: P1=12min, P2=30min, P3=60min, P4=120min
                double diff = switch (i.getPriority()) {
                    case "P1" -> 0.2;
                    case "P2" -> 0.5;
                    case "P3" -> 1.0;
                    default -> 2.0;
                };
                totalMttaHours += diff;
                mttaCount++;
            }
        }
        double mtta = mttaCount == 0 ? 0.0 : totalMttaHours / mttaCount;

        // Priority Distribution
        Map<String, Long> priorityDist = incidents.stream()
                .collect(Collectors.groupingBy(Incident::getPriority, Collectors.counting()));

        // Status Distribution
        Map<String, Long> statusDist = incidents.stream()
                .collect(Collectors.groupingBy(Incident::getStatus, Collectors.counting()));

        // Ensure all priorities exist in distribution map
        List.of("P1", "P2", "P3", "P4").forEach(p -> priorityDist.putIfAbsent(p, 0L));
        List.of("OPEN", "ASSIGNED", "IN_PROGRESS", "PENDING", "RESOLVED", "CLOSED", "CANCELLED")
