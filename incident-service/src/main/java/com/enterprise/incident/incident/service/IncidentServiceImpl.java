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

        if (query != null && !query.trim().isEmpty()) {
            String lowerQuery = "%" + query.toLowerCase() + "%";
            spec = spec.and((root, cq, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("title")), lowerQuery),
                            cb.like(cb.lower(root.get("incidentNumber")), lowerQuery)
                    )
            );
        }

        if (priority != null) {
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("priority"), priority));
        }

        if (status != null) {
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("status"), status));
        }

        if (assigneeId != null) {
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("assigneeId"), assigneeId));
        }

        if (startDate != null) {
            spec = spec.and((root, cq, cb) -> cb.greaterThanOrEqualTo(root.get("createdDate"), startDate));
        }

        if (endDate != null) {
            spec = spec.and((root, cq, cb) -> cb.lessThanOrEqualTo(root.get("createdDate"), endDate));
        }

        // Return DTO mapped results
        Page<Incident> page = incidentRepository.findAll(spec, pageable);
        return page.map(i -> mapToDto(i, token));
    }

    @Override
    @Transactional
    public void deleteIncident(Long id, String token) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Incident not found"));
        incidentRepository.delete(incident);
    }

    @Override
    @Transactional
    public void processSlaEscalations() {
        log.info("Running SLA background scheduler scanner...");
        List<Incident> activeIncidents = incidentRepository.findBySlaBreachedFalseAndStatusNotIn(
                List.of(Status.RESOLVED, Status.CLOSED, Status.CANCELLED)
        );

        LocalDateTime now = LocalDateTime.now();
        for (Incident incident : activeIncidents) {
            if (incident.getSlaDueDate() != null && now.isAfter(incident.getSlaDueDate())) {
                incident.setSlaBreached(true);
                incident.setEscalated(true);
                
                int nextEscalationLevel = incident.getEscalationLevel() + 1;
                incident.setEscalationLevel(nextEscalationLevel);

                // Auto Assign Escalation Owner level logic:
                // Level 1: P1 -> Team Lead (we fetch the lead id for this team if assignee has one, or default lead)
                // Level 2: P1 overdue -> Incident Manager
                // Level 3: P1 severely overdue -> Admin
                // Here we write audit logs and update assignee level
                createAuditLog(incident.getId(), "SLA_BREACHED", 0L, "false", "true");
                createAuditLog(incident.getId(), "ESCALATION_LEVEL", 0L, 
                        String.valueOf(nextEscalationLevel - 1), String.valueOf(nextEscalationLevel));

                incidentRepository.save(incident);
                sendKafkaEvent("INCIDENT_ESCALATED", incident);
                log.info("Incident {} breached SLA! Escalation level bumped to {}.", incident.getIncidentNumber(), nextEscalationLevel);
            }
        }
    }

    private String generateIncidentNumber() {
        try {
            Long val = redisTemplate.opsForValue().increment(INCIDENT_NUMBER_KEY);
            return "INC-" + String.format("%05d", val);
        } catch (Exception e) {
            // Database Max ID fallback
            long count = incidentRepository.count();
            return "INC-" + String.format("%05d", count + 1);
        }
    }

    private LocalDateTime calculateSlaDueDate(Priority priority) {
        Optional<SlaRule> rule = slaRuleRepository.findByPriority(priority);
        long minutes = 24 * 60; // 24 Hours default
        if (rule.isPresent()) {
            minutes = rule.get().getResolutionTimeMinutes();
        } else {
            // Standard seed times: P1=1h, P2=4h, P3=24h, P4=72h
            minutes = switch (priority) {
                case P1 -> 60;
                case P2 -> 4 * 60;
                case P3 -> 24 * 60;
                case P4 -> 72 * 60;
            };
        }
        return LocalDateTime.now().plusMinutes(minutes);
    }

    private void checkAndAudit(Incident incident, String field, String oldVal, String newVal, Long userId) {
        if (newVal != null && !newVal.equals(oldVal)) {
            createAuditLog(incident.getId(), field, userId, oldVal, newVal);
        }
    }

    private void createAuditLog(Long incidentId, String action, Long userId, String oldValue, String newValue) {
        AuditLog logEntity = AuditLog.builder()
                .incidentId(incidentId)
                .action(action)
                .changedBy(userId)
                .oldValue(oldValue)
                .newValue(newValue)
                .build();
        auditLogRepository.save(logEntity);
    }

    private void sendKafkaEvent(String eventType, Incident incident) {
        try {
            // Build a clean event string
            String eventJson = String.format(
                    "{\"eventType\":\"%s\",\"incidentId\":%d,\"incidentNumber\":\"%s\",\"priority\":\"%s\",\"status\":\"%s\",\"assigneeId\":%d}",
                    eventType, incident.getId(), incident.getIncidentNumber(), incident.getPriority().name(), incident.getStatus().name(),
                    incident.getAssigneeId() != null ? incident.getAssigneeId() : 0L
            );
            kafkaTemplate.send(KAFKA_TOPIC, incident.getIncidentNumber(), eventJson);
        } catch (Exception e) {
            log.error("Failed to publish Kafka event for Incident {}: {}", incident.getIncidentNumber(), e.getMessage());
        }
    }

    private UserDto fetchUserSafe(Long userId, String token) {
        if (userId == null) return null;
        try {
            return authServiceClient.getUserById(userId, token);
        } catch (Exception e) {
            log.error("Could not fetch user ID {} via Feign: {}", userId, e.getMessage());
            return UserDto.builder().id(userId).username("user_id_" + userId).email("unknown@enterprise.com").build();
        }
    }

    private IncidentDto mapToDto(Incident incident, String token) {
        String assigneeName = "Unassigned";
        if (incident.getAssigneeId() != null) {
            UserDto assignee = fetchUserSafe(incident.getAssigneeId(), token);
            if (assignee != null) assigneeName = assignee.getUsername();
        }

        String reporterName = "Unknown";
        if (incident.getReporterId() != null) {
            UserDto reporter = fetchUserSafe(incident.getReporterId(), token);
            if (reporter != null) reporterName = reporter.getUsername();
        }

        return IncidentDto.builder()
                .id(incident.getId())
                .incidentNumber(incident.getIncidentNumber())
                .title(incident.getTitle())
                .description(incident.getDescription())
                .category(incident.getCategory())
                .subcategory(incident.getSubcategory())
                .priority(incident.getPriority())
                .severity(incident.getSeverity())
                .status(incident.getStatus())
                .assigneeId(incident.getAssigneeId())
                .assigneeName(assigneeName)
                .reporterId(incident.getReporterId())
                .reporterName(reporterName)
                .createdDate(incident.getCreatedDate())
                .resolvedDate(incident.getResolvedDate())
                .closedDate(incident.getClosedDate())
                .slaDueDate(incident.getSlaDueDate())
                .slaBreached(incident.isSlaBreached())
                .escalated(incident.isEscalated())
                .escalationLevel(incident.getEscalationLevel())
                .version(incident.getVersion())
                .build();
    }
}
