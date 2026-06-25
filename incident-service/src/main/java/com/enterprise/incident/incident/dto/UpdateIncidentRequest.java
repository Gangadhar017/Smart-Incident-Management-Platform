package com.enterprise.incident.incident.dto;

import com.enterprise.incident.incident.entity.Priority;
import com.enterprise.incident.incident.entity.Status;
import lombok.Data;

@Data
public class UpdateIncidentRequest {
    private String title;
    private String description;
    private Priority priority;
    private String severity;
    private Status status;
    private Long assigneeId;
}
