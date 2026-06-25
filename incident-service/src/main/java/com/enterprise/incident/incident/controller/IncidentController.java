package com.enterprise.incident.incident.controller;

import com.enterprise.incident.incident.dto.CreateIncidentRequest;
import com.enterprise.incident.incident.dto.IncidentDto;
import com.enterprise.incident.incident.dto.UpdateIncidentRequest;
import com.enterprise.incident.incident.entity.Priority;
import com.enterprise.incident.incident.entity.Status;
import com.enterprise.incident.incident.service.IncidentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/incidents")
@RequiredArgsConstructor
public class IncidentController {

    private final IncidentService incidentService;

    @PostMapping
    public ResponseEntity<IncidentDto> createIncident(
            @Valid @RequestBody CreateIncidentRequest request,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        return ResponseEntity.ok(incidentService.createIncident(request, token));
    }

    @PutMapping("/{id}")
    public ResponseEntity<IncidentDto> updateIncident(
            @PathVariable Long id,
            @RequestBody UpdateIncidentRequest request,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        return ResponseEntity.ok(incidentService.updateIncident(id, request, token));
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncidentDto> getIncidentById(
            @PathVariable Long id,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        return ResponseEntity.ok(incidentService.getIncidentById(id, token));
    }

    @GetMapping("/number/{incidentNumber}")
    public ResponseEntity<IncidentDto> getIncidentByNumber(
            @PathVariable String incidentNumber,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        return ResponseEntity.ok(incidentService.getIncidentByNumber(incidentNumber, token));
    }

    @GetMapping
    public ResponseEntity<Page<IncidentDto>> getIncidents(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {

        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(incidentService.searchIncidents(
                search, priority, status, assigneeId, departmentId, startDate, endDate, pageable, token
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIncident(
            @PathVariable Long id,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        incidentService.deleteIncident(id, token);
        return ResponseEntity.noContent().build();
    }
}
