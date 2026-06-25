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
