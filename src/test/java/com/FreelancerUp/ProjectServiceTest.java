package com.FreelancerUp;

import com.FreelancerUp.exception.BadRequestException;
import com.FreelancerUp.exception.ResourceNotFoundException;
import com.FreelancerUp.feature.common.dto.ProjectBudgetDTO;
import com.FreelancerUp.feature.project.dto.request.CreateProjectRequest;
import com.FreelancerUp.feature.project.dto.request.ProjectSearchRequest;
import com.FreelancerUp.feature.project.dto.request.UpdateProjectRequest;
import com.FreelancerUp.feature.project.dto.response.ProjectDetailResponse;
import com.FreelancerUp.feature.project.dto.response.ProjectResponse;
import com.FreelancerUp.feature.project.service.impl.ProjectServiceImpl;
import com.FreelancerUp.model.document.Project;
import com.FreelancerUp.model.entity.Client;
import com.FreelancerUp.model.entity.User;
import com.FreelancerUp.model.enums.ProjectStatus;
import com.FreelancerUp.model.enums.ProjectType;
import com.FreelancerUp.feature.project.repository.ProjectRepository;
import com.FreelancerUp.feature.client.repository.ClientRepository;
import com.FreelancerUp.feature.user.repository.UserRepository;
import com.FreelancerUp.cache.RedisCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Project Service Tests")
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private RedisCacheService redisCacheService;

    private ProjectServiceImpl projectServiceImpl;

    private UUID clientId;
    private User user;
    private Client client;
    private Project project;

    @BeforeEach
    void setUp() {
        projectServiceImpl = new ProjectServiceImpl(
                projectRepository,
                userRepository,
                clientRepository,
                redisCacheService
        );

        clientId = UUID.randomUUID();

        user = User.builder()
                .id(clientId)
                .email("client@example.com")
                .fullName("John Doe")
                .avatarUrl("http://example.com/avatar.jpg")
                .build();

        client = Client.builder()
                .id(clientId)
                .companyName("Tech Corp")
                .postedProjects(0)
                .build();

        project = Project.builder()
                .id("project123")
                .clientId(clientId.toString())
                .title("Build Mobile App")
                .description("Need a mobile app built")
                .requirements("React Native")
                .skills(List.of("React Native", "iOS", "Android"))
                .budget(Project.ProjectBudget.builder()
                        .minAmount(BigDecimal.valueOf(5000))
                        .maxAmount(BigDecimal.valueOf(10000))
                        .currency("USD")
                        .isNegotiable(true)
                        .build())
                .duration(60)
                .status(ProjectStatus.OPEN)
                .type(ProjectType.FIXED_PRICE)
                .deadline(LocalDateTime.now().plusDays(60))
                .build();
    }

    @Test
    @DisplayName("Should create project successfully")
    void testCreateProject_Success() {
        // Given
        CreateProjectRequest request = CreateProjectRequest.builder()
                .title("Build Mobile App")
                .description("Need a mobile app built")
                .requirements("React Native")
                .skills(List.of("React Native", "iOS", "Android"))
                .budget(ProjectBudgetDTO.builder()
                        .minAmount(BigDecimal.valueOf(5000))
                        .maxAmount(BigDecimal.valueOf(10000))
                        .currency("USD")
                        .isNegotiable(true)
                        .build())
                .duration(60)
                .type(ProjectType.FIXED_PRICE)
                .deadline(LocalDateTime.now().plusDays(60))
                .build();

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(userRepository.findById(clientId)).thenReturn(Optional.of(user));
        when(redisCacheService.keys("projects:list:*")).thenReturn(java.util.Set.of());

        // When
        ProjectResponse response = projectServiceImpl.createProject(clientId, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo("project123");
        assertThat(response.getTitle()).isEqualTo("Build Mobile App");
        assertThat(response.getStatus()).isEqualTo(ProjectStatus.OPEN);

        verify(clientRepository).findById(clientId);
        verify(projectRepository).save(any(Project.class));
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when client not found during project creation")
    void testCreateProject_ClientNotFound() {
        // Given
        CreateProjectRequest request = CreateProjectRequest.builder()
                .title("Build Mobile App")
                .build();

        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> projectServiceImpl.createProject(clientId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Client not found");

        verify(clientRepository).findById(clientId);
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    @DisplayName("Should search projects successfully")
    void testSearchProjects_Success() {
        // Given
        ProjectSearchRequest request = ProjectSearchRequest.builder()
                .keyword("mobile")
                .page(0)
                .size(10)
                .sortBy("createdAt")
                .sortDirection("desc")
                .build();

        Page<Project> projectPage = new PageImpl<>(List.of(project));
        when(projectRepository.findAll(any(Pageable.class))).thenReturn(projectPage);
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(user));

        // When
        Page<ProjectResponse> response = projectServiceImpl.searchProjects(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getTitle()).isEqualTo("Build Mobile App");

        verify(projectRepository).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Should get project detail successfully")
    void testGetProjectDetail_Success() {
        // Given
        when(projectRepository.findById("project123")).thenReturn(Optional.of(project));
        when(userRepository.findById(clientId)).thenReturn(Optional.of(user));

        // When
        ProjectDetailResponse response = projectServiceImpl.getProjectDetail("project123");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo("project123");
        assertThat(response.getTitle()).isEqualTo("Build Mobile App");
        assertThat(response.getClientFullName()).isEqualTo("John Doe");

        verify(projectRepository).findById("project123");
        verify(userRepository).findById(clientId);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when project not found")
    void testGetProjectDetail_ProjectNotFound() {
        // Given
        when(projectRepository.findById("invalid")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> projectServiceImpl.getProjectDetail("invalid"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Project not found");

        verify(projectRepository).findById("invalid");
        verify(userRepository, never()).findById(any(UUID.class));
    }

    @Test
    @DisplayName("Should update project successfully")
    void testUpdateProject_Success() {
        // Given
        UpdateProjectRequest request = UpdateProjectRequest.builder()
                .title("Updated Title")
                .duration(90)
                .build();

        when(projectRepository.findById("project123")).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(userRepository.findById(clientId)).thenReturn(Optional.of(user));
        when(redisCacheService.keys("projects:list:*")).thenReturn(java.util.Set.of());

        // When
        ProjectResponse response = projectServiceImpl.updateProject("project123", request);

        // Then
        assertThat(response).isNotNull();
        assertThat(project.getTitle()).isEqualTo("Updated Title");
        assertThat(project.getDuration()).isEqualTo(90);

        verify(projectRepository).findById("project123");
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    @DisplayName("Should update project status successfully")
    void testUpdateProjectStatus_Success() {
        // Given
        when(projectRepository.findById("project123")).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(userRepository.findById(clientId)).thenReturn(Optional.of(user));
        when(redisCacheService.keys("projects:list:*")).thenReturn(java.util.Set.of());

        // When
        ProjectResponse response = projectServiceImpl.updateProjectStatus("project123", ProjectStatus.IN_PROGRESS);

        // Then
        assertThat(response).isNotNull();
        assertThat(project.getStatus()).isEqualTo(ProjectStatus.IN_PROGRESS);
        assertThat(project.getStartedAt()).isNotNull();

        verify(projectRepository).findById("project123");
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    @DisplayName("Should throw BadRequestException for invalid status transition")
    void testUpdateProjectStatus_InvalidTransition() {
        // Given
        project.setStatus(ProjectStatus.COMPLETED);
        when(projectRepository.findById("project123")).thenReturn(Optional.of(project));

        // When & Then
        assertThatThrownBy(() -> projectServiceImpl.updateProjectStatus("project123", ProjectStatus.OPEN))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Cannot transition from COMPLETED to OPEN");

        verify(projectRepository).findById("project123");
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    @DisplayName("Should delete project successfully")
    void testDeleteProject_Success() {
        // Given
        when(projectRepository.findById("project123")).thenReturn(Optional.of(project));
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(redisCacheService.keys("projects:list:*")).thenReturn(java.util.Set.of());
        doNothing().when(projectRepository).deleteById("project123");

        // When
        projectServiceImpl.deleteProject("project123", clientId);

        // Then
        verify(projectRepository).findById("project123");
        verify(clientRepository).findById(clientId);
        verify(projectRepository).deleteById("project123");
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    @DisplayName("Should throw BadRequestException when deleting non-owned project")
    void testDeleteProject_NotOwner() {
        // Given
        UUID differentClientId = UUID.randomUUID();
        when(projectRepository.findById("project123")).thenReturn(Optional.of(project));

        // When & Then
        assertThatThrownBy(() -> projectServiceImpl.deleteProject("project123", differentClientId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("You can only delete your own projects");

        verify(projectRepository).findById("project123");
        verify(projectRepository, never()).deleteById("project123");
    }

    @Test
    @DisplayName("Should throw BadRequestException when deleting non-OPEN project")
    void testDeleteProject_NotOpen() {
        // Given
        project.setStatus(ProjectStatus.IN_PROGRESS);
        when(projectRepository.findById("project123")).thenReturn(Optional.of(project));

        // When & Then
        assertThatThrownBy(() -> projectServiceImpl.deleteProject("project123", clientId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Can only delete projects with OPEN status");

        verify(projectRepository).findById("project123");
        verify(projectRepository, never()).deleteById("project123");
    }
}
