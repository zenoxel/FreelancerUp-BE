package com.FreelancerUp.feature.project.service;

import com.FreelancerUp.feature.project.dto.request.CreateProjectRequest;
import com.FreelancerUp.feature.project.dto.request.ProjectSearchRequest;
import com.FreelancerUp.feature.project.dto.request.UpdateProjectRequest;
import com.FreelancerUp.feature.project.dto.response.ProjectDetailResponse;
import com.FreelancerUp.feature.project.dto.response.ProjectResponse;
import com.FreelancerUp.model.enums.ProjectStatus;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface ProjectService {

    ProjectResponse createProject(UUID clientId, CreateProjectRequest request);

    ProjectResponse updateProject(String projectId, UpdateProjectRequest request);

    ProjectResponse updateProjectStatus(String projectId, ProjectStatus status);

    ProjectDetailResponse getProjectDetail(String projectId);

    Page<ProjectResponse> searchProjects(ProjectSearchRequest request);

    void deleteProject(String projectId, UUID clientId);
}
