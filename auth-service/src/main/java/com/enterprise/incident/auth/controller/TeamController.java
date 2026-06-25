package com.enterprise.incident.auth.controller;

import com.enterprise.incident.auth.dto.TeamDto;
import com.enterprise.incident.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
public class TeamController {

    private final AuthService authService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<TeamDto> createTeam(@RequestBody TeamDto dto) {
        return ResponseEntity.ok(authService.createTeam(dto));
    }

    @GetMapping
    @RequestMapping("/department/{departmentId}")
    public ResponseEntity<List<TeamDto>> getTeamsByDepartment(@PathVariable Long departmentId) {
        return ResponseEntity.ok(authService.getTeamsByDepartment(departmentId));
    }
}
