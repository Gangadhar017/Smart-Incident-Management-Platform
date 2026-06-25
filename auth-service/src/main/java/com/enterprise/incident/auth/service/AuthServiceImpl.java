package com.enterprise.incident.auth.service;

import com.enterprise.incident.auth.dto.*;
import com.enterprise.incident.auth.entity.Department;
import com.enterprise.incident.auth.entity.Role;
import com.enterprise.incident.auth.entity.Team;
import com.enterprise.incident.auth.entity.User;
import com.enterprise.incident.auth.repository.DepartmentRepository;
import com.enterprise.incident.auth.repository.TeamRepository;
import com.enterprise.incident.auth.repository.UserRepository;
import com.enterprise.incident.auth.security.JwtUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final TeamRepository teamRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userDetails.getUsername()));

        String accessToken = jwtUtils.generateAccessToken(userDetails, user.getId(), user.getRole().name());
        String refreshToken = jwtUtils.generateRefreshToken(userDetails);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public TokenResponse refresh(RefreshTokenRequest request) {
        String token = request.getRefreshToken();
        String username = jwtUtils.getUsernameFromToken(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (!jwtUtils.validateToken(token, userDetails)) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        String newAccess = jwtUtils.generateAccessToken(userDetails, user.getId(), user.getRole().name());
        String newRefresh = jwtUtils.generateRefreshToken(userDetails);

        return TokenResponse.builder()
                .accessToken(newAccess)
                .refreshToken(newRefresh)
                .build();
    }

    @Override
    @Transactional
    public UserDto createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        Department department = null;
        if (request.getDepartmentId() != null) {
            department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new EntityNotFoundException("Department not found"));
        }

        Team team = null;
        if (request.getTeamId() != null) {
            team = teamRepository.findById(request.getTeamId())
                    .orElseThrow(() -> new EntityNotFoundException("Team not found"));
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .department(department)
                .team(team)
                .skills(request.getSkills())
                .active(true)
                .build();

        User saved = userRepository.save(user);
        return mapToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return mapToDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        return mapToDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> searchUsers(String query, Long departmentId, Pageable pageable) {
        Specification<User> spec = Specification.where(null);

        if (query != null && !query.trim().isEmpty()) {
            String lowerQuery = "%" + query.toLowerCase() + "%";
            spec = spec.and((root, criteriaQuery, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("username")), lowerQuery),
                            cb.like(cb.lower(root.get("email")), lowerQuery)
                    )
            );
        }

        if (departmentId != null) {
            spec = spec.and((root, criteriaQuery, cb) ->
                    cb.equal(root.get("department").get("id"), departmentId)
            );
        }

        return userRepository.findAll(spec, pageable).map(this::mapToDto);
    }

    @Override
    @Transactional
    public DepartmentDto createDepartment(DepartmentDto dto) {
        if (departmentRepository.existsByCode(dto.getCode())) {
            throw new IllegalArgumentException("Department code already exists");
        }

        Department department = Department.builder()
                .name(dto.getName())
                .code(dto.getCode())
                .managerId(dto.getManagerId())
                .build();

        Department saved = departmentRepository.save(department);
        dto.setId(saved.getId());
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentDto> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(d -> {
                    DepartmentDto dto = new DepartmentDto();
                    dto.setId(d.getId());
                    dto.setName(d.getName());
                    dto.setCode(d.getCode());
                    dto.setManagerId(d.getManagerId());
                    return dto;
                }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TeamDto createTeam(TeamDto dto) {
        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new EntityNotFoundException("Department not found"));

        Team team = Team.builder()
                .name(dto.getName())
                .department(department)
                .leadId(dto.getLeadId())
                .build();

        Team saved = teamRepository.save(team);
        dto.setId(saved.getId());
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamDto> getTeamsByDepartment(Long departmentId) {
        return teamRepository.findByDepartmentId(departmentId).stream()
                .map(t -> {
                    TeamDto dto = new TeamDto();
                    dto.setId(t.getId());
                    dto.setName(t.getName());
                    dto.setDepartmentId(t.getDepartment().getId());
                    dto.setLeadId(t.getLeadId());
                    return dto;
                }).collect(Collectors.toList());
    }

    private UserDto mapToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .active(user.isActive())
                .departmentId(user.getDepartment() != null ? user.getDepartment().getId() : null)
                .departmentName(user.getDepartment() != null ? user.getDepartment().getName() : null)
                .teamId(user.getTeam() != null ? user.getTeam().getId() : null)
                .teamName(user.getTeam() != null ? user.getTeam().getName() : null)
                .skills(user.getSkills())
                .build();
    }
}
