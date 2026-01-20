package com.FreelancerUp.feature.project.controller;

import com.FreelancerUp.feature.project.dto.request.CreateProjectRequest;
import com.FreelancerUp.feature.project.dto.request.ProjectSearchRequest;
import com.FreelancerUp.feature.project.dto.request.UpdateProjectRequest;
import com.FreelancerUp.feature.project.dto.response.ProjectDetailResponse;
import com.FreelancerUp.feature.project.dto.response.ProjectResponse;
import com.FreelancerUp.feature.project.service.ProjectService;
import com.FreelancerUp.model.enums.ProjectStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Tag(name = "Project", description = "Project management APIs")
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Create a new project", description = "Client can create a new project")
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody CreateProjectRequest request) {
        // TODO: Get clientId from authentication context in Phase 12
        // For now, this endpoint will be updated after security implementation
        log.info("REST request to create project");
        // This is a temporary placeholder - will be fixed in Phase 12
        throw new UnsupportedOperationException("Will be implemented after security configuration");
    }

    @GetMapping("/search")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Search projects", description = "Search and filter projects with pagination")
    public ResponseEntity<Page<ProjectResponse>> searchProjects(ProjectSearchRequest request) {
        log.info("REST request to search projects with filters: {}", request.getKeyword());
        Page<ProjectResponse> response = projectService.searchProjects(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{projectId}")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Get project details", description = "Get detailed information about a specific project")
    public ResponseEntity<ProjectDetailResponse> getProjectDetail(
            @Parameter(description = "Project ID", required = true)
            @PathVariable String projectId) {
        log.info("REST request to get project detail: {}", projectId);
        ProjectDetailResponse response = projectService.getProjectDetail(projectId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{projectId}")
    @PreAuthorize("hasRole('CLIENT') and @projectSecurity.isOwner(#projectId, authentication)")
    @Operation(summary = "Update project", description = "Update project information (owner only)")
    public ResponseEntity<ProjectResponse> updateProject(
            @Parameter(description = "Project ID", required = true)
            @PathVariable String projectId,
            @Valid @RequestBody UpdateProjectRequest request) {
        log.info("REST request to update project: {}", projectId);
        ProjectResponse response = projectService.updateProject(projectId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{projectId}/status")
    @PreAuthorize("hasRole('CLIENT') and @projectSecurity.isOwner(#projectId, authentication)")
    @Operation(summary = "Update project status", description = "Update project status (owner only)")
    public ResponseEntity<ProjectResponse> updateProjectStatus(
            @Parameter(description = "Project ID", required = true)
            @PathVariable String projectId,
            @Parameter(description = "New status", required = true)
            @RequestParam ProjectStatus status) {
        log.info("REST request to update project status: {} -> {}", projectId, status);
        ProjectResponse response = projectService.updateProjectStatus(projectId, status);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{projectId}")
    @PreAuthorize("hasRole('CLIENT') and @projectSecurity.isOwner(#projectId, authentication)")
    @Operation(summary = "Delete project", description = "Delete a project (owner only, OPEN status only)")
    public ResponseEntity<Void> deleteProject(
            @Parameter(description = "Project ID", required = true)
            @PathVariable String projectId,
            @Parameter(description = "Client ID (from authentication)", required = true)
            @RequestParam UUID clientId) {
        log.info("REST request to delete project: {}", projectId);
        projectService.deleteProject(projectId, clientId);
        return ResponseEntity.noContent().build();
    }
}
