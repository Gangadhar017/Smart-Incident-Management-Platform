package com.enterprise.incident.incident.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sla_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlaRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private Priority priority;

    @Column(name = "response_time_minutes", nullable = false)
    private long responseTimeMinutes;

    @Column(name = "resolution_time_minutes", nullable = false)
    private long resolutionTimeMinutes;
}
