package com.enterprise.incident.incident.repository;

import com.enterprise.incident.incident.entity.Incident;
import com.enterprise.incident.incident.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long>, JpaSpecificationExecutor<Incident> {
    Optional<Incident> findByIncidentNumber(String incidentNumber);
    List<Incident> findBySlaBreachedFalseAndStatusNotIn(List<Status> statuses);
}
