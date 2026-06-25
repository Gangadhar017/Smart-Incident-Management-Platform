package com.enterprise.incident.incident.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDto {
    private Long id;
    private Long incidentId;
    private String action;
    private Long changedBy;
    private String changedByName;
    private String oldValue;
    private String newValue;
    private LocalDateTime timestamp;
}
