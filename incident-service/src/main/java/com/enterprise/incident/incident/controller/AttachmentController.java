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
            @RequestParam("file") MultipartFile file,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token) throws IOException {
        return ResponseEntity.ok(attachmentService.uploadFile(
                incidentId, file.getOriginalFilename(), file.getContentType(), file.getBytes(), token
        ));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadFile(
            @PathVariable Long id,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        byte[] data = attachmentService.downloadFile(id, token);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"file_" + id + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }

    @GetMapping("/incident/{incidentId}")
    public ResponseEntity<List<AttachmentDto>> getAttachments(
            @PathVariable Long incidentId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        return ResponseEntity.ok(attachmentService.getAttachmentsForIncident(incidentId, token));
    }
}
