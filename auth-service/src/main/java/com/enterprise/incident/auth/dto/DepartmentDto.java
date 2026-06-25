package com.enterprise.incident.auth.dto;

import lombok.Data;

@Data
public class DepartmentDto {
    private Long id;
    private String name;
    private String code;
    private Long managerId;
}
