package com.enterprise.incident.auth.config;

import com.enterprise.incident.auth.entity.Department;
import com.enterprise.incident.auth.entity.Role;
import com.enterprise.incident.auth.entity.Team;
import com.enterprise.incident.auth.entity.User;
import com.enterprise.incident.auth.repository.DepartmentRepository;
import com.enterprise.incident.auth.repository.TeamRepository;
import com.enterprise.incident.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final TeamRepository teamRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0) {
            return; // Data already initialized
        }

        // 1. Seed Departments
        Department itDept = departmentRepository.save(Department.builder().name("Information Technology").code("IT").build());
        Department opsDept = departmentRepository.save(Department.builder().name("Infrastructure Operations").code("OPS").build());
        Department supportDept = departmentRepository.save(Department.builder().name("Customer Support").code("CS").build());

        // 2. Seed Teams
        Team serviceDesk = teamRepository.save(Team.builder().name("Service Desk").department(itDept).build());
        Team cloudTeam = teamRepository.save(Team.builder().name("DevOps & Cloud").department(opsDept).build());
        Team l2Support = teamRepository.save(Team.builder().name("L2 Support").department(supportDept).build());

        // 3. Seed Users
        User superAdmin = userRepository.save(User.builder()
                .username("superadmin")
                .email("superadmin@enterprise.com")
                .password(passwordEncoder.encode("adminpassword"))
                .role(Role.SUPER_ADMIN)
                .department(itDept)
                .active(true)
                .build());

        User admin = userRepository.save(User.builder()
                .username("admin")
                .email("admin@enterprise.com")
                .password(passwordEncoder.encode("adminpassword"))
                .role(Role.ADMIN)
                .department(itDept)
                .active(true)
                .build());

        User manager = userRepository.save(User.builder()
                .username("manager")
                .email("manager@enterprise.com")
                .password(passwordEncoder.encode("adminpassword"))
                .role(Role.INCIDENT_MANAGER)
                .department(itDept)
                .active(true)
                .build());

        User lead = userRepository.save(User.builder()
                .username("lead")
                .email("lead@enterprise.com")
                .password(passwordEncoder.encode("adminpassword"))
                .role(Role.TEAM_LEAD)
                .department(opsDept)
                .team(cloudTeam)
                .active(true)
                .build());

        User engineer = userRepository.save(User.builder()
                .username("engineer")
                .email("engineer@enterprise.com")
                .password(passwordEncoder.encode("adminpassword"))
                .role(Role.SUPPORT_ENGINEER)
                .department(opsDept)
                .team(cloudTeam)
                .skills(Set.of("Java", "PostgreSQL", "Redis", "Kafka", "AWS"))
                .active(true)
                .build());

        User employee = userRepository.save(User.builder()
                .username("employee")
                .email("employee@enterprise.com")
                .password(passwordEncoder.encode("adminpassword"))
                .role(Role.EMPLOYEE)
                .department(supportDept)
                .team(l2Support)
                .active(true)
                .build());

        // Update managers and leads references
        itDept.setManagerId(manager.getId());
        departmentRepository.save(itDept);

        opsDept.setManagerId(manager.getId());
        departmentRepository.save(opsDept);

        cloudTeam.setLeadId(lead.getId());
        teamRepository.save(cloudTeam);
    }
}
