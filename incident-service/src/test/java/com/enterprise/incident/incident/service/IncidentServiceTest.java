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
