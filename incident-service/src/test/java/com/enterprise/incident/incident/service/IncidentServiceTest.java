package com.enterprise.incident.incident.service;

import com.enterprise.incident.incident.client.AuthServiceClient;
import com.enterprise.incident.incident.dto.CreateIncidentRequest;
import com.enterprise.incident.incident.dto.IncidentDto;
import com.enterprise.incident.incident.dto.UserDto;
import com.enterprise.incident.incident.entity.Incident;
import com.enterprise.incident.incident.entity.Priority;
import com.enterprise.incident.incident.entity.Status;
import com.enterprise.incident.incident.repository.AuditLogRepository;
import com.enterprise.incident.incident.repository.IncidentRepository;
import com.enterprise.incident.incident.repository.SlaRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IncidentServiceTest {

    @Mock
    private IncidentRepository incidentRepository;
    @Mock
    private SlaRuleRepository slaRuleRepository;
    @Mock
    private AuditLogRepository auditLogRepository;
    @Mock
    private AuthServiceClient authServiceClient;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private IncidentServiceImpl incidentService;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private UserDto reporter;

    @BeforeEach
    void setUp() {
        reporter = UserDto.builder()
                .id(2L)
                .username("employee1")
                .email("emp@enterprise.com")
                .role("EMPLOYEE")
                .build();
    }

    @Test
    void testCreateIncident_Success() {
        CreateIncidentRequest req = new CreateIncidentRequest();
        req.setTitle("Database Connection Failure");
        req.setDescription("Latency spikes on DB server");
        req.setCategory("Database");
        req.setPriority(Priority.P1);
        req.setSeverity("Critical");
        req.setReporterId(2L);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("imp:incident:number")).thenReturn(100L);
        when(authServiceClient.getUserById(eq(2L), anyString())).thenReturn(reporter);
        when(slaRuleRepository.findByPriority(Priority.P1)).thenReturn(Optional.empty());

        when(incidentRepository.save(any(Incident.class))).thenAnswer(inv -> {
            Incident i = inv.getArgument(0);
            i.setId(1L);
            i.setCreatedDate(LocalDateTime.now());
            return i;
        });

        IncidentDto dto = incidentService.createIncident(req, "Bearer mock_token");

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("INC-00100", dto.getIncidentNumber());
        assertEquals(Priority.P1, dto.getPriority());
        assertEquals(Status.OPEN, dto.getStatus());
        assertNotNull(dto.getSlaDueDate());
        verify(incidentRepository, times(1)).save(any());
        verify(auditLogRepository, times(1)).save(any());
        verify(kafkaTemplate, times(1)).send(anyString(), anyString(), anyString());
    }

    @Test
    void testProcessSlaEscalations_Breached_TriggersEscalation() {
        Incident breachedIncident = Incident.builder()
                .id(1L)
                .incidentNumber("INC-00001")
                .title("System Offline")
                .status(Status.ASSIGNED)
                .priority(Priority.P1)
                .slaDueDate(LocalDateTime.now().minusMinutes(5)) // overdue by 5 mins
                .slaBreached(false)
                .escalationLevel(0)
                .build();

        when(incidentRepository.findBySlaBreachedFalseAndStatusNotIn(anyList()))
                .thenReturn(List.of(breachedIncident));

        incidentService.processSlaEscalations();

        assertTrue(breachedIncident.isSlaBreached());
        assertTrue(breachedIncident.isEscalated());
        assertEquals(1, breachedIncident.getEscalationLevel());
        verify(incidentRepository, times(1)).save(breachedIncident);
        verify(auditLogRepository, atLeastOnce()).save(any());
        verify(kafkaTemplate, times(1)).send(anyString(), anyString(), anyString());
    }
}
