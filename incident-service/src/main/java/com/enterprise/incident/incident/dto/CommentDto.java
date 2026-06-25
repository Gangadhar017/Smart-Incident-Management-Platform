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
public class CommentDto {
    private Long id;
    private Long incidentId;
    private Long authorId;
    private String authorName;
    private String content;
    private boolean internal;
    private LocalDateTime createdDate;
}
