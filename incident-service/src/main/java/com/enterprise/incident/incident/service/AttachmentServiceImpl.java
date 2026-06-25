package com.enterprise.incident.incident.service;

import com.enterprise.incident.incident.client.AuthServiceClient;
import com.enterprise.incident.incident.dto.AttachmentDto;
import com.enterprise.incident.incident.dto.UserDto;
import com.enterprise.incident.incident.entity.Attachment;
import com.enterprise.incident.incident.entity.Incident;
import com.enterprise.incident.incident.repository.AttachmentRepository;
import com.enterprise.incident.incident.repository.IncidentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttachmentServiceImpl implements AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final IncidentRepository incidentRepository;
    private final AuthServiceClient authServiceClient;

    private static final String UPLOAD_DIR = "uploads";

    @Override
    @Transactional
    public AttachmentDto uploadFile(Long incidentId, String filename, String contentType, byte[] data, String token) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new EntityNotFoundException("Incident not found"));

        UserDto user = authServiceClient.getCurrentUser(token);
        Long userId = user != null ? user.getId() : 0L;

        // Generate S3 Mock Key
        String s3Key = UUID.randomUUID().toString() + "_" + filename;

        try {
            // Ensure local upload directory exists
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Save file physically
            Path filePath = uploadPath.resolve(s3Key);
            Files.write(filePath, data);

            Attachment attachment = Attachment.builder()
                    .incident(incident)
                    .filename(filename)
