package com.enterprise.incident.incident.dto;

import com.enterprise.incident.incident.entity.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateIncidentRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Category is required")
    private String category;

    private String subcategory;

    @NotNull(message = "Priority is required")
    private Priority priority;

    @NotBlank(message = "Severity is required")
    private String severity;

    @NotNull(message = "Reporter ID is required")
    private Long reporterId;
}
