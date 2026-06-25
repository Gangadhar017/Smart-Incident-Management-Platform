package com.enterprise.incident.incident.dto;

import lombok.Data;

@Data
public class SlaRuleDto {
    private Long id;
    private String priority;
    private long responseTimeMinutes;
    private long resolutionTimeMinutes;
}
