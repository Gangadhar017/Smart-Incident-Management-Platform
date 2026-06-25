package com.enterprise.incident.reporting.service;

import com.enterprise.incident.reporting.dto.DashboardKpiDto;

public interface ReportingService {
    DashboardKpiDto getDashboardKpis();
    byte[] generatePdfReport();
    byte[] generateExcelReport();
}
