package com.enterprise.incident.incident.repository;

import com.enterprise.incident.incident.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByIncidentIdOrderByTimestampDesc(Long incidentId);
}
