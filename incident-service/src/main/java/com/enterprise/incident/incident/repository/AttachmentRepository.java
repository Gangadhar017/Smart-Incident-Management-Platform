package com.enterprise.incident.incident.repository;

import com.enterprise.incident.incident.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    List<Attachment> findByIncidentId(Long incidentId);
}
