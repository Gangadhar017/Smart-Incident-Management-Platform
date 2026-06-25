package com.enterprise.incident.auth.service;

import com.enterprise.incident.auth.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    TokenResponse refresh(RefreshTokenRequest request);
    UserDto createUser(CreateUserRequest request);
    UserDto getUserProfile(String username);
    UserDto getUserById(Long id);
    Page<UserDto> searchUsers(String query, Long departmentId, Pageable pageable);
    
    // Departments
    DepartmentDto createDepartment(DepartmentDto dto);
    List<DepartmentDto> getAllDepartments();
    
    // Teams
    TeamDto createTeam(TeamDto dto);
    List<TeamDto> getTeamsByDepartment(Long departmentId);
}
