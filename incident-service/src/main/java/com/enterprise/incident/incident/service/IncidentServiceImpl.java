package com.enterprise.incident.incident.service;

import com.enterprise.incident.incident.client.AuthServiceClient;
import com.enterprise.incident.incident.dto.CreateIncidentRequest;
import com.enterprise.incident.incident.dto.IncidentDto;
import com.enterprise.incident.incident.dto.UpdateIncidentRequest;
import com.enterprise.incident.incident.dto.UserDto;
import com.enterprise.incident.incident.entity.*;
import com.enterprise.incident.incident.repository.AuditLogRepository;
import com.enterprise.incident.incident.repository.IncidentRepository;
import com.enterprise.incident.incident.repository.SlaRuleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class IncidentServiceImpl implements IncidentService {

    private final IncidentRepository incidentRepository;
    private final SlaRuleRepository slaRuleRepository;
    private final AuditLogRepository auditLogRepository;
    private final AuthServiceClient authServiceClient;
    private final StringRedisTemplate redisTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String INCIDENT_NUMBER_KEY = "imp:incident:number";
    private static final String KAFKA_TOPIC = "incident-events";

    @Override
    @Transactional
    public IncidentDto createIncident(CreateIncidentRequest request, String token) {
        // 1. Generate unique Incident Number (Redis counter + database fallback)
        String incidentNum = generateIncidentNumber();

        // 2. Validate reporter
        UserDto reporter = fetchUserSafe(request.getReporterId(), token);
        if (reporter == null) {
            throw new IllegalArgumentException("Invalid reporter ID");
        }

        // 3. Compute SLA Due Date based on priority
        LocalDateTime slaDueDate = calculateSlaDueDate(request.getPriority());

        // 4. Save Incident
        Incident incident = Incident.builder()
                .incidentNumber(incidentNum)
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .subcategory(request.getSubcategory())
                .priority(request.getPriority())
                .severity(request.getSeverity())
                .status(Status.OPEN)
                .reporterId(request.getReporterId())
                .slaDueDate(slaDueDate)
                .slaBreached(false)
                .escalated(false)
                .escalationLevel(0)
                .build();

        Incident saved = incidentRepository.save(incident);

        // 5. Generate Audit Log
        createAuditLog(saved.getId(), "CREATED", request.getReporterId(), null, "Created Incident " + incidentNum);

        // 6. Publish Event to Kafka
        sendKafkaEvent("INCIDENT_CREATED", saved);

        return mapToDto(saved, token);
    }

    @Override
    @Transactional
    public IncidentDto updateIncident(Long id, UpdateIncidentRequest request, String token) {
        String lockKey = "lock:incident:" + id;
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "locked", Duration.ofSeconds(5));
        if (acquired == null || !acquired) {
            throw new IllegalStateException("Incident is currently being updated. Please try again.");
        }

        try {
            Incident incident = incidentRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Incident not found"));

            UserDto currentUser = authServiceClient.getCurrentUser(token);
            Long updaterId = currentUser != null ? currentUser.getId() : 0L;

            // Track changes for auditing
            checkAndAudit(incident, "title", incident.getTitle(), request.getTitle(), updaterId);
            checkAndAudit(incident, "description", incident.getDescription(), request.getDescription(), updaterId);
            checkAndAudit(incident, "priority", incident.getPriority() != null ? incident.getPriority().name() : null,
                    request.getPriority() != null ? request.getPriority().name() : null, updaterId);
            checkAndAudit(incident, "severity", incident.getSeverity(), request.getSeverity(), updaterId);
            checkAndAudit(incident, "status", incident.getStatus() != null ? incident.getStatus().name() : null,
                    request.getStatus() != null ? request.getStatus().name() : null, updaterId);
            checkAndAudit(incident, "assigneeId", incident.getAssigneeId() != null ? String.valueOf(incident.getAssigneeId()) : null,
                    request.getAssigneeId() != null ? String.valueOf(request.getAssigneeId()) : null, updaterId);

            // Update parameters
            if (request.getTitle() != null) incident.setTitle(request.getTitle());
            if (request.getDescription() != null) incident.setDescription(request.getDescription());
            if (request.getPriority() != null) {
                incident.setPriority(request.getPriority());
                // Recalculate SLA due date if priority changes
                incident.setSlaDueDate(calculateSlaDueDate(request.getPriority()));
            }
            if (request.getSeverity() != null) incident.setSeverity(request.getSeverity());
            
            if (request.getStatus() != null) {
                Status oldStatus = incident.getStatus();
                incident.setStatus(request.getStatus());
                if (request.getStatus() == Status.RESOLVED && oldStatus != Status.RESOLVED) {
                    incident.setResolvedDate(LocalDateTime.now());
                } else if (request.getStatus() == Status.CLOSED && oldStatus != Status.CLOSED) {
                    incident.setClosedDate(LocalDateTime.now());
                }
            }

            if (request.getAssigneeId() != null) {
                // If previously open, transition status to ASSIGNED
                incident.setAssigneeId(request.getAssigneeId());
                if (incident.getStatus() == Status.OPEN) {
                    incident.setStatus(Status.ASSIGNED);
                    createAuditLog(incident.getId(), "status", updaterId, "OPEN", "ASSIGNED");
                }
            }

            Incident saved = incidentRepository.save(incident);

            // Publish Event
            sendKafkaEvent("INCIDENT_UPDATED", saved);

            return mapToDto(saved, token);
        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public IncidentDto getIncidentById(Long id, String token) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Incident not found"));
        return mapToDto(incident, token);
    }

    @Override
    @Transactional(readOnly = true)
    public IncidentDto getIncidentByNumber(String incidentNumber, String token) {
        Incident incident = incidentRepository.findByIncidentNumber(incidentNumber)
                .orElseThrow(() -> new EntityNotFoundException("Incident number not found"));
        return mapToDto(incident, token);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<IncidentDto> searchIncidents(
            String query,
            Priority priority,
            Status status,
            Long assigneeId,
            Long departmentId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable,
            String token
    ) {
        Specification<Incident> spec = Specification.where(null);

