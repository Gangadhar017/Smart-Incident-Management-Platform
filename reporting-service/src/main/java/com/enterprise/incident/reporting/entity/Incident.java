package com.enterprise.incident.reporting.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "incidents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Incident {

    @Id
    private Long id;

    @Column(name = "incident_number")
    private String incidentNumber;

    private String title;

    private String priority;

    private String status;

    @Column(name = "assignee_id")
    private Long assigneeId;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "resolved_date")
    private LocalDateTime resolvedDate;

    @Column(name = "closed_date")
    private LocalDateTime closedDate;

    @Column(name = "sla_due_date")
    private LocalDateTime slaDueDate;

    @Column(name = "sla_breached")
    private boolean slaBreached;
}
