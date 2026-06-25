package com.enterprise.incident.reporting.controller;

import com.enterprise.incident.reporting.dto.DashboardKpiDto;
import com.enterprise.incident.reporting.service.ReportingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReportingController {

    private final ReportingService reportingService;

    @GetMapping("/analytics/dashboard")
    public ResponseEntity<DashboardKpiDto> getDashboardData() {
