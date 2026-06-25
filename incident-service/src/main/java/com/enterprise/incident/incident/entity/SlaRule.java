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

