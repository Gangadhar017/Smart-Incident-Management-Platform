package com.enterprise.incident.incident.controller;

import com.enterprise.incident.incident.dto.AuditLogDto;
import com.enterprise.incident.incident.entity.AuditLog;
import com.enterprise.incident.incident.repository.AuditLogRepository;
import com.enterprise.incident.incident.client.AuthServiceClient;
import com.enterprise.incident.incident.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/incidents")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;
    private final AuthServiceClient authServiceClient;

    @GetMapping("/{incidentId}/audit")
    public ResponseEntity<List<AuditLogDto>> getIncidentAudit(
            @PathVariable Long incidentId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        List<AuditLog> logs = auditLogRepository.findByIncidentIdOrderByTimestampDesc(incidentId);
        List<AuditLogDto> dtos = logs.stream().map(log -> {
            String userName = "System";
            if (log.getChangedBy() != 0) {
                try {
                    UserDto user = authServiceClient.getUserById(log.getChangedBy(), token);
                    if (user != null) userName = user.getUsername();
                } catch (Exception e) {
                    // Ignore Feign fetch failures
                }
            }
            return AuditLogDto.builder()
                    .id(log.getId())
                    .incidentId(log.getIncidentId())
                    .action(log.getAction())
                    .changedBy(log.getChangedBy())
                    .changedByName(userName)
                    .oldValue(log.getOldValue())
                    .newValue(log.getNewValue())
                    .timestamp(log.getTimestamp())
                    .build();
        }).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }
}
