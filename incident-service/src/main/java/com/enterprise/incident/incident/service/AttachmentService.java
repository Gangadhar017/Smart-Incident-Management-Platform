package com.enterprise.incident.incident.service;

import com.enterprise.incident.incident.dto.AttachmentDto;

import java.util.List;

public interface AttachmentService {
    AttachmentDto uploadFile(Long incidentId, String filename, String contentType, byte[] data, String token);
    byte[] downloadFile(Long attachmentId, String token);
    List<AttachmentDto> getAttachmentsForIncident(Long incidentId, String token);
}
