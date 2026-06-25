package com.enterprise.incident.incident.service;

import com.enterprise.incident.incident.client.AuthServiceClient;
import com.enterprise.incident.incident.dto.CommentDto;
import com.enterprise.incident.incident.dto.UserDto;
import com.enterprise.incident.incident.entity.Comment;
import com.enterprise.incident.incident.entity.Incident;
import com.enterprise.incident.incident.repository.CommentRepository;
import com.enterprise.incident.incident.repository.IncidentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final IncidentRepository incidentRepository;
    private final AuthServiceClient authServiceClient;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Override
    @Transactional
    public CommentDto addComment(Long incidentId, CommentDto commentDto, String token) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new EntityNotFoundException("Incident not found"));

        UserDto author = authServiceClient.getCurrentUser(token);
        Long authorId = author != null ? author.getId() : 0L;

        Comment comment = Comment.builder()
                .incident(incident)
                .authorId(authorId)
                .content(commentDto.getContent())
                .internal(commentDto.isInternal())
                .build();

        Comment saved = commentRepository.save(comment);

        // Check for mentions (e.g., @john)
        detectAndProcessMentions(incident, commentDto.getContent(), author.getUsername());

        // Publish Comment Added Event
        try {
            String eventJson = String.format(
                    "{\"eventType\":\"COMMENT_ADDED\",\"incidentId\":%d,\"incidentNumber\":\"%s\",\"authorId\":%d,\"isInternal\":%b}",
                    incident.getId(), incident.getIncidentNumber(), authorId, comment.isInternal()
