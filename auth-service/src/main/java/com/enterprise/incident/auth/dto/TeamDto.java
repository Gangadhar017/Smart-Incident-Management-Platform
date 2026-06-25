package com.enterprise.incident.auth.dto;

import lombok.Data;

@Data
public class TeamDto {
    private Long id;
    private String name;
    private Long departmentId;
    private Long leadId;
}
