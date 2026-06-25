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
                case "INCIDENT_ESCALATED":
                    subject = "[IMP] SLA Breach Escalation: " + incidentNumber;
                    body = "Incident " + incidentNumber + " has breached SLA and has been escalated to next response level.";
                    break;
                case "COMMENT_ADDED":
                    subject = "[IMP] Comment Posted: " + incidentNumber;
                    body = "A comment has been added to Incident " + incidentNumber;
                    break;
                case "USER_MENTIONED":
                    String mentionedUser = root.has("mentionedUser") ? root.get("mentionedUser").asText() : "";
                    String byUser = root.has("byUser") ? root.get("byUser").asText() : "";
                    subject = "[IMP] Mention Alert in Incident " + incidentNumber;
                    body = "You were mentioned in Incident " + incidentNumber + " by @" + byUser + ". Content check required.";
                    log.info("Alerting mentioned user: {}", mentionedUser);
                    break;
                default:
                    subject = "[IMP] Event Alert";
                    body = "An event of type " + eventType + " occurred in Incident " + incidentNumber;
            }

            // Route all notifications to support mailbox in mock environment
            sendEmail("operations-alerts@enterprise.com", subject, body);

        } catch (Exception e) {
            log.error("Failed to parse Kafka JSON message or dispatch email: {}", e.getMessage());
        }
    }

    private void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("no-reply@enterprise-imp.com");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Notification email sent successfully to {} with subject: {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to dispatch email via SMTP server: {}", e.getMessage());
        }
    }
}
