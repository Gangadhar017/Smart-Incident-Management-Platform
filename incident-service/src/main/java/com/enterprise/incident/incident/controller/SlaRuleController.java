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
public class SlaRuleController {

    private final SlaRuleRepository slaRuleRepository;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<SlaRule> createSlaRule(@RequestBody SlaRule rule) {
        return ResponseEntity.ok(slaRuleRepository.save(rule));
    }

    @GetMapping
    public ResponseEntity<List<SlaRule>> getAllSlaRules() {
        return ResponseEntity.ok(slaRuleRepository.findAll());
    }
}
