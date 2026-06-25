package com.enterprise.incident.incident.controller;

import com.enterprise.incident.incident.dto.CommentDto;
import com.enterprise.incident.incident.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/incident/{incidentId}")
    public ResponseEntity<CommentDto> addComment(
            @PathVariable Long incidentId,
            @RequestBody CommentDto commentDto,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        return ResponseEntity.ok(commentService.addComment(incidentId, commentDto, token));
    }

    @GetMapping("/incident/{incidentId}")
    public ResponseEntity<List<CommentDto>> getComments(
            @PathVariable Long incidentId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        return ResponseEntity.ok(commentService.getCommentsForIncident(incidentId, token));
    }
}
