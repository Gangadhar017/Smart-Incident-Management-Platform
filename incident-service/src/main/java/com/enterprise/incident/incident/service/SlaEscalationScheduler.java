package com.enterprise.incident.incident.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SlaEscalationScheduler {

    private final IncidentService incidentService;

    // Scan every 30 seconds
    @Scheduled(fixedDelay = 30000)
    public void scanBreaches() {
        try {
            incidentService.processSlaEscalations();
        } catch (Exception e) {
            log.error("Error occurred during background SLA escalation processing: {}", e.getMessage());
        }
    }
}
