package com.enterprise.incident.auth.service;

import com.enterprise.incident.auth.dto.CreateUserRequest;
import com.enterprise.incident.auth.dto.LoginRequest;
import com.enterprise.incident.auth.dto.LoginResponse;
import com.enterprise.incident.auth.dto.UserDto;
import com.enterprise.incident.auth.entity.Role;
import com.enterprise.incident.auth.entity.User;
import com.enterprise.incident.auth.repository.DepartmentRepository;
import com.enterprise.incident.auth.repository.TeamRepository;
import com.enterprise.incident.auth.repository.UserRepository;
import com.enterprise.incident.auth.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private UserRepository userRepository;
    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testengineer")
                .email("engineer@enterprise.com")
                .password("encoded_pass")
                .role(Role.SUPPORT_ENGINEER)
                .active(true)
                .build();
    }
