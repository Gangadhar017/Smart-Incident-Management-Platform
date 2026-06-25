package com.enterprise.incident.notification.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final JavaMailSender mailSender;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "incident-events", groupId = "notification-group")
    public void consumeIncidentEvent(String message) {
        log.info("Received Kafka incident event message: {}", message);
        try {
            JsonNode root = objectMapper.readTree(message);
            String eventType = root.get("eventType").asText();
            String incidentNumber = root.has("incidentNumber") ? root.get("incidentNumber").asText() : "N/A";

            String subject = "";
            String body = "";

            switch (eventType) {
                case "INCIDENT_CREATED":
                    subject = "[IMP] Incident Created: " + incidentNumber;
                    body = "A new production incident " + incidentNumber + " has been logged in the system. Priority: " +
                            (root.has("priority") ? root.get("priority").asText() : "N/A");
                    break;
                case "INCIDENT_UPDATED":
                    subject = "[IMP] Incident Updated: " + incidentNumber;
                    body = "Incident " + incidentNumber + " has been updated. Status: " +
                            (root.has("status") ? root.get("status").asText() : "N/A");
                    break;
