package com.enterprise.incident.incident.service;

import com.enterprise.incident.incident.dto.CommentDto;

import java.util.List;

public interface CommentService {
    CommentDto addComment(Long incidentId, CommentDto commentDto, String token);
    List<CommentDto> getCommentsForIncident(Long incidentId, String token);
}
