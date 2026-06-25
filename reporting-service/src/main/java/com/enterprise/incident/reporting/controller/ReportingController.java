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
        return ResponseEntity.ok(reportingService.getDashboardKpis());
    }

    @GetMapping("/reports/pdf")
    public ResponseEntity<byte[]> downloadPdfReport() {
        byte[] pdfData = reportingService.generatePdfReport();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"incident_report.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfData);
    }

    @GetMapping("/reports/excel")
    public ResponseEntity<byte[]> downloadExcelReport() {
        byte[] excelData = reportingService.generateExcelReport();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"incident_report.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(excelData);
    }
}
