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

    @Test
    void testLogin_Success() {
        LoginRequest req = new LoginRequest();
        req.setUsername("testengineer");
        req.setPassword("password");

        Authentication auth = mock(Authentication.class);
        UserDetails details = mock(UserDetails.class);
        when(details.getUsername()).thenReturn("testengineer");
        when(auth.getPrincipal()).thenReturn(details);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);

        when(userRepository.findByUsername("testengineer")).thenReturn(Optional.of(testUser));
        when(jwtUtils.generateAccessToken(any(), any(), any())).thenReturn("mock_access");
        when(jwtUtils.generateRefreshToken(any())).thenReturn("mock_refresh");

        LoginResponse response = authService.login(req);

        assertNotNull(response);
        assertEquals("mock_access", response.getAccessToken());
        assertEquals("testengineer", response.getUsername());
        assertEquals("SUPPORT_ENGINEER", response.getRole());
        verify(authenticationManager, times(1)).authenticate(any());
    }

    @Test
    void testCreateUser_Success() {
        CreateUserRequest req = new CreateUserRequest();
        req.setUsername("newuser");
        req.setEmail("newuser@enterprise.com");
        req.setPassword("pass123");
        req.setRole(Role.EMPLOYEE);
        req.setSkills(Set.of("Java"));

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@enterprise.com")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("hashedPass");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(2L);
            return u;
        });

        UserDto created = authService.createUser(req);

        assertNotNull(created);
        assertEquals(2L, created.getId());
        assertEquals("newuser", created.getUsername());
        assertEquals("EMPLOYEE", created.getRole());
        assertTrue(created.getSkills().contains("Java"));
    }

    @Test
    void testCreateUser_UsernameExists_ThrowsException() {
        CreateUserRequest req = new CreateUserRequest();
        req.setUsername("testengineer");
        req.setEmail("unique@enterprise.com");

        when(userRepository.existsByUsername("testengineer")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> authService.createUser(req));
    }
}
