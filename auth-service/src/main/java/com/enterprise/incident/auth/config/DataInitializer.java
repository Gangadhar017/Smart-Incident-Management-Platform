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
