package com.enterprise.incident.incident;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.enterprise.incident.incident.client")
@EnableJpaAuditing
@EnableScheduling
public class IncidentApplication {
    public static void main(String[] args) {
        SpringApplication.run(IncidentApplication.class, args);
    }
}
