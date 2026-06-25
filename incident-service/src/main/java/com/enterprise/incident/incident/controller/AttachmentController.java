package com.enterprise.incident.incident.controller;

import com.enterprise.incident.incident.dto.AttachmentDto;
import com.enterprise.incident.incident.service.AttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/attachments")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping("/incident/{incidentId}")
    public ResponseEntity<AttachmentDto> uploadFile(
            @PathVariable Long incidentId,
