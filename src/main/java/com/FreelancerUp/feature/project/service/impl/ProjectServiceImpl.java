package com.FreelancerUp.feature.project.service.impl;

import com.FreelancerUp.exception.BadRequestException;
import com.FreelancerUp.exception.ResourceNotFoundException;
import com.FreelancerUp.feature.client.repository.ClientRepository;
import com.FreelancerUp.feature.project.dto.request.CreateProjectRequest;
import com.FreelancerUp.feature.project.dto.request.ProjectSearchRequest;
import com.FreelancerUp.feature.project.dto.request.UpdateProjectRequest;
import com.FreelancerUp.feature.project.dto.response.ProjectDetailResponse;
import com.FreelancerUp.feature.project.dto.response.ProjectResponse;
import com.FreelancerUp.feature.project.service.ProjectService;
import com.FreelancerUp.feature.common.dto.ProjectBudgetDTO;
import com.FreelancerUp.model.document.Project;
import com.FreelancerUp.model.entity.Client;
import com.FreelancerUp.model.entity.User;
import com.FreelancerUp.model.enums.ProjectStatus;
import com.FreelancerUp.model.enums.ProjectType;
import com.FreelancerUp.feature.project.repository.ProjectRepository;
import com.FreelancerUp.feature.user.repository.UserRepository;
import com.FreelancerUp.cache.RedisCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final RedisCacheService redisCacheService;

    @Override
    public ProjectResponse createProject(UUID clientId, CreateProjectRequest request) {
        log.info("Creating project for client: {}", clientId);

        // Validate client exists
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

        // Create project
        Project.ProjectBudget budget = convertToBudget(request.getBudget());

        Project project = Project.builder()
                .clientId(clientId.toString())
                .title(request.getTitle())
                .description(request.getDescription())
                .requirements(request.getRequirements())
                .skills(request.getSkills())
                .budget(budget)
                .duration(request.getDuration())
                .status(ProjectStatus.OPEN)
                .type(request.getType())
                .deadline(request.getDeadline())
                .build();

        project = projectRepository.save(project);

        // Update client stats
        client.setPostedProjects(client.getPostedProjects() + 1);
        clientRepository.save(client);

        // Clear cache
        invalidateProjectListCache();

        User clientUser = userRepository.findById(clientId).orElse(null);

        log.info("Project created successfully with ID: {}", project.getId());
        return convertToResponse(project, clientUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectResponse> searchProjects(ProjectSearchRequest request) {
        log.info("Searching projects with filters: {}", request.getKeyword());

        // TODO: Implement caching in Phase 11
        // Build dynamic query
        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(Sort.Direction.fromString(request.getSortDirection()), request.getSortBy())
        );

        // Simple implementation - return all projects with basic filtering
        // TODO: Implement Specification pattern for complex queries in Phase 5
        Page<Project> projects = projectRepository.findAll(pageable);

        return projects.map(p -> {
            User client = userRepository.findById(UUID.fromString(p.getClientId()))
                    .orElse(null);
            return convertToResponse(p, client);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectDetailResponse getProjectDetail(String projectId) {
        log.info("Fetching project detail for ID: {}", projectId);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        UUID clientId = UUID.fromString(project.getClientId());
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

        User freelancer = null;
        if (project.getFreelancerId() != null) {
            freelancer = userRepository.findById(UUID.fromString(project.getFreelancerId()))
                    .orElse(null);
        }

        return convertToDetailResponse(project, client, freelancer);
    }

    @Override
    public ProjectResponse updateProject(String projectId, UpdateProjectRequest request) {
        log.info("Updating project: {}", projectId);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        // Update fields if provided
        if (request.getTitle() != null) {
            project.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        if (request.getRequirements() != null) {
            project.setRequirements(request.getRequirements());
        }
        if (request.getSkills() != null && !request.getSkills().isEmpty()) {
            project.setSkills(request.getSkills());
        }
        if (request.getBudget() != null) {
            project.setBudget(convertToBudget(request.getBudget()));
        }
        if (request.getDuration() != null) {
            project.setDuration(request.getDuration());
        }
        if (request.getType() != null) {
            project.setType(request.getType());
        }
        if (request.getDeadline() != null) {
            project.setDeadline(request.getDeadline());
        }

        project = projectRepository.save(project);
        invalidateProjectListCache();

        User client = userRepository.findById(UUID.fromString(project.getClientId())).orElse(null);

        log.info("Project updated successfully: {}", projectId);
        return convertToResponse(project, client);
    }

    @Override
    public ProjectResponse updateProjectStatus(String projectId, ProjectStatus status) {
        log.info("Updating project status: {} -> {}", projectId, status);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        // Validate status transitions
        validateStatusTransition(project.getStatus(), status);

        project.setStatus(status);

        if (status == ProjectStatus.IN_PROGRESS) {
            project.setStartedAt(LocalDateTime.now());
        } else if (status == ProjectStatus.COMPLETED) {
            project.setCompletedAt(LocalDateTime.now());
        }

        project = projectRepository.save(project);
        invalidateProjectListCache();

        User client = userRepository.findById(UUID.fromString(project.getClientId())).orElse(null);

        log.info("Project status updated successfully: {}", projectId);
        return convertToResponse(project, client);
    }

    @Override
    public void deleteProject(String projectId, UUID clientId) {
        log.info("Deleting project: {}", projectId);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        // Verify ownership
        if (!project.getClientId().equals(clientId.toString())) {
            throw new BadRequestException("You can only delete your own projects");
        }

        // Can only delete open projects
        if (project.getStatus() != ProjectStatus.OPEN) {
            throw new BadRequestException("Can only delete projects with OPEN status");
        }

        projectRepository.deleteById(projectId);

        // Update client stats
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));
        client.setPostedProjects(Math.max(0, client.getPostedProjects() - 1));
        clientRepository.save(client);

        invalidateProjectListCache();

        log.info("Project deleted successfully: {}", projectId);
    }

    // Helper methods
    private Project.ProjectBudget convertToBudget(ProjectBudgetDTO dto) {
        if (dto == null) {
            return new Project.ProjectBudget();
        }

        return Project.ProjectBudget.builder()
                .minAmount(dto.getMinAmount())
                .maxAmount(dto.getMaxAmount())
                .currency(dto.getCurrency())
                .isNegotiable(dto.getIsNegotiable())
                .build();
    }

    private ProjectResponse convertToResponse(Project project, User client) {
        return ProjectResponse.builder()
                .id(project.getId())
                .clientId(project.getClientId())
                .clientName(client != null ? client.getFullName() : null)
                .clientAvatarUrl(client != null ? client.getAvatarUrl() : null)
                .title(project.getTitle())
                .description(project.getDescription())
                .skills(project.getSkills())
                .budget(convertBudgetToDTO(project.getBudget()))
                .duration(project.getDuration())
                .status(project.getStatus())
                .type(project.getType())
                .deadline(project.getDeadline())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }

    private ProjectBudgetDTO convertBudgetToDTO(Project.ProjectBudget budget) {
        if (budget == null) {
            return null;
        }

        return ProjectBudgetDTO.builder()
                .minAmount(budget.getMinAmount())
                .maxAmount(budget.getMaxAmount())
                .currency(budget.getCurrency())
                .isNegotiable(budget.getIsNegotiable())
                .build();
    }

    private ProjectDetailResponse convertToDetailResponse(Project project, User client, User freelancer) {
        ProjectDetailResponse.ProjectDetailResponseBuilder builder = ProjectDetailResponse.builder()
                .id(project.getId())
                .clientId(project.getClientId())
                .title(project.getTitle())
                .description(project.getDescription())
                .requirements(project.getRequirements())
                .skills(project.getSkills())
                .budget(convertBudgetToDTO(project.getBudget()))
                .duration(project.getDuration())
                .status(project.getStatus())
                .type(project.getType())
                .deadline(project.getDeadline())
                .startedAt(project.getStartedAt())
                .completedAt(project.getCompletedAt())
                .contractId(project.getContractId())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt());

        if (client != null) {
            builder
                .clientIdUUID(client.getId())
                .clientEmail(client.getEmail())
                .clientFullName(client.getFullName())
                .clientAvatarUrl(client.getAvatarUrl())
                    .clientCompanyName("Not set"); // TODO: Get from Client profile
        }

        if (freelancer != null) {
            builder
                .freelancerIdUUID(freelancer.getId())
                .freelancerEmail(freelancer.getEmail())
                .freelancerFullName(freelancer.getFullName())
                .freelancerAvatarUrl(freelancer.getAvatarUrl());
        }

        // TODO: Implement statistics after Bid module is ready
        builder
                .totalBids(0)
                .averageBidAmount(0);

        return builder.build();
    }

    private void validateStatusTransition(ProjectStatus from, ProjectStatus to) {
        // Define allowed transitions
        Map<ProjectStatus, List<ProjectStatus>> allowedTransitions = Map.of(
                ProjectStatus.OPEN, List.of(ProjectStatus.IN_PROGRESS, ProjectStatus.COMPLETED),
                ProjectStatus.IN_PROGRESS, List.of(ProjectStatus.COMPLETED),
                ProjectStatus.COMPLETED, List.of()
        );

        List<ProjectStatus> allowed = allowedTransitions.get(from);
        if (allowed == null || !allowed.contains(to)) {
            throw new BadRequestException(
                    String.format("Cannot transition from %s to %s", from, to)
            );
        }
    }

    private void invalidateProjectListCache() {
        // Invalidate all project list caches
        Set<String> keys = redisCacheService.keys("projects:list:*");
        keys.forEach(key -> redisCacheService.delete(key));
    }
}
