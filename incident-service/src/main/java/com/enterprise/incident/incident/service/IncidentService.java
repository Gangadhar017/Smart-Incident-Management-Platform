package com.enterprise.incident.incident.service;

import com.enterprise.incident.incident.dto.CreateIncidentRequest;
import com.enterprise.incident.incident.dto.IncidentDto;
import com.enterprise.incident.incident.dto.UpdateIncidentRequest;
import com.enterprise.incident.incident.entity.Priority;
import com.enterprise.incident.incident.entity.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface IncidentService {
    IncidentDto createIncident(CreateIncidentRequest request, String token);
    IncidentDto updateIncident(Long id, UpdateIncidentRequest request, String token);
    IncidentDto getIncidentById(Long id, String token);
    IncidentDto getIncidentByNumber(String incidentNumber, String token);
    Page<IncidentDto> searchIncidents(
            String query,
            Priority priority,
            Status status,
            Long assigneeId,
            Long departmentId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable,
            String token
    );
    void deleteIncident(Long id, String token);
    void processSlaEscalations();
}
