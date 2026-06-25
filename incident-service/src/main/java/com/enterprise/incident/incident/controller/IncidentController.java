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
