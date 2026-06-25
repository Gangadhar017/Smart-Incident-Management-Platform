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
