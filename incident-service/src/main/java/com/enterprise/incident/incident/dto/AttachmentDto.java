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
public class AttachmentDto {
    private Long id;
    private Long incidentId;
    private String filename;
    private String s3Key;
    private long fileSize;
    private String contentType;
    private Long uploadedBy;
    private String uploadedByName;
    private LocalDateTime uploadedDate;
}
