package com.enterprise.incident.incident.controller;

import com.enterprise.incident.incident.entity.SlaRule;
import com.enterprise.incident.incident.repository.SlaRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sla-rules")
@RequiredArgsConstructor
