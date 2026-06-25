package com.enterprise.incident.incident.dto;

import com.enterprise.incident.incident.entity.Priority;
import com.enterprise.incident.incident.entity.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentDto {
    private Long id;
    private String incidentNumber;
    private String title;
    private String description;
    private String category;
    private String subcategory;
    private Priority priority;
    private String severity;
    private Status status;
    private Long assigneeId;
    private String assigneeName;
    private Long reporterId;
    private String reporterName;
    private LocalDateTime createdDate;
    private LocalDateTime resolvedDate;
    private LocalDateTime closedDate;
    private LocalDateTime slaDueDate;
    private boolean slaBreached;
    private boolean escalated;
    private int escalationLevel;
    private Long version;
}
