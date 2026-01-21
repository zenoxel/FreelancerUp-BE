package com.FreelancerUp;

import com.FreelancerUp.exception.BadRequestException;
import com.FreelancerUp.exception.ResourceNotFoundException;
import com.FreelancerUp.feature.chat.dto.response.ConversationResponse;
import com.FreelancerUp.feature.chat.repository.ConversationRepository;
import com.FreelancerUp.feature.chat.service.impl.ConversationServiceImpl;
import com.FreelancerUp.feature.project.repository.ProjectRepository;
import com.FreelancerUp.feature.user.repository.UserRepository;
import com.FreelancerUp.model.document.Project;
import com.FreelancerUp.model.entity.Conversation;
import com.FreelancerUp.model.entity.User;
import com.FreelancerUp.model.enums.ProjectStatus;
import com.FreelancerUp.model.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ConversationService.
 *
 * Tests the conversation lifecycle:
 * 1. Get or create conversation for project
 * 2. Retrieve conversation by ID and project
 * 3. Get user conversations
 * 4. Update last message info
 * 5. Manage participants
 * 6. Deactivate conversation
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Conversation Service Tests")
class ConversationServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ConversationServiceImpl conversationService;

    private UUID clientId;
    private UUID freelancerId;
    private String projectId;
    private UUID conversationId;
    private User clientUser;
    private Project project;
    private Conversation conversation;

    @BeforeEach
    void setUp() {
        clientId = UUID.randomUUID();
        freelancerId = UUID.randomUUID();
        projectId = "507f1f77bcf86cd799439011";
        conversationId = UUID.randomUUID();

        // Setup users
        clientUser = User.builder()
                .id(clientId)
                .email("client@example.com")
                .fullName("Client User")
                .role(Role.CLIENT)
                .build();

        // Setup project
        project = Project.builder()
                .id(projectId)
                .title("Web Development Project")
                .description("Build a website")
                .clientId(clientId.toString())
                .status(ProjectStatus.OPEN)
                .build();

        // Setup conversation
        conversation = Conversation.builder()
                .id(conversationId)
                .projectId(projectId)
                .participantIds(new ArrayList<>(List.of(clientId, freelancerId)))
                .lastMessageAt(LocalDateTime.now())
                .lastMessagePreview("Last message preview")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should return existing conversation")
    void testGetOrCreateConversation_Existing() {
        when(conversationRepository.findByProjectId(projectId)).thenReturn(Optional.of(conversation));

        ConversationResponse response = conversationService.getOrCreateConversation(projectId, clientId);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(conversationId);
        assertThat(response.getProjectId()).isEqualTo(projectId);

        verify(conversationRepository, never()).save(any(Conversation.class));
    }

    @Test
    @DisplayName("Should create new conversation when not exists")
    void testGetOrCreateConversation_New() {
        when(conversationRepository.findByProjectId(projectId)).thenReturn(Optional.empty());
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(userRepository.findById(clientId)).thenReturn(Optional.of(clientUser));
        when(conversationRepository.save(any(Conversation.class))).thenAnswer(invocation -> {
            Conversation conv = invocation.getArgument(0);
            conv.setId(conversationId);
            conv.setCreatedAt(LocalDateTime.now());
            conv.setUpdatedAt(LocalDateTime.now());
            return conv;
        });

        ConversationResponse response = conversationService.getOrCreateConversation(projectId, clientId);

        assertThat(response).isNotNull();
        assertThat(response.getProjectId()).isEqualTo(projectId);
        assertThat(response.getProjectTitle()).isEqualTo("Web Development Project");
        assertThat(response.getParticipantIds()).containsExactly(clientId);
        assertThat(response.getIsActive()).isTrue();

        verify(conversationRepository, times(1)).save(any(Conversation.class));
    }

    @Test
    @DisplayName("Should throw exception when project not found")
    void testGetOrCreateConversation_ProjectNotFound() {
        when(conversationRepository.findByProjectId(projectId)).thenReturn(Optional.empty());
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> conversationService.getOrCreateConversation(projectId, clientId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Project not found");

        verify(conversationRepository, never()).save(any(Conversation.class));
    }

    @Test
    @DisplayName("Should throw exception when client not found")
    void testGetOrCreateConversation_ClientNotFound() {
        when(conversationRepository.findByProjectId(projectId)).thenReturn(Optional.empty());
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(userRepository.findById(clientId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> conversationService.getOrCreateConversation(projectId, clientId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Client not found");

        verify(conversationRepository, never()).save(any(Conversation.class));
    }

    @Test
    @DisplayName("Should get conversation by ID successfully")
    void testGetConversationById_Success() {
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));

        ConversationResponse response = conversationService.getConversationById(conversationId);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(conversationId);
        assertThat(response.getProjectId()).isEqualTo(projectId);

        verify(conversationRepository, times(1)).findById(conversationId);
    }

    @Test
    @DisplayName("Should throw exception when conversation not found by ID")
    void testGetConversationById_NotFound() {
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> conversationService.getConversationById(conversationId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Conversation not found");
    }

    @Test
    @DisplayName("Should get conversation by project ID successfully")
    void testGetConversationByProjectId_Success() {
        when(conversationRepository.findByProjectId(projectId)).thenReturn(Optional.of(conversation));

        ConversationResponse response = conversationService.getConversationByProjectId(projectId);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(conversationId);
        assertThat(response.getProjectId()).isEqualTo(projectId);

        verify(conversationRepository, times(1)).findByProjectId(projectId);
    }

    @Test
    @DisplayName("Should throw exception when conversation not found by project")
    void testGetConversationByProjectId_NotFound() {
        when(conversationRepository.findByProjectId(projectId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> conversationService.getConversationByProjectId(projectId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Conversation not found for this project");
    }

    @Test
    @DisplayName("Should get conversations for user successfully")
    void testGetConversationsForUser_Success() {
        List<Conversation> conversations = List.of(conversation);
        when(conversationRepository.findActiveConversationsForUser(clientId)).thenReturn(conversations);

        List<ConversationResponse> responses = conversationService.getConversationsForUser(clientId);

        assertThat(responses).isNotNull();
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getId()).isEqualTo(conversationId);

        verify(conversationRepository, times(1)).findActiveConversationsForUser(clientId);
    }

    @Test
    @DisplayName("Should update last message successfully")
    void testUpdateLastMessage_Success() {
        String newPreview = "New message preview";
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(conversationRepository.save(any(Conversation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        conversationService.updateLastMessage(conversationId, newPreview);

        assertThat(conversation.getLastMessagePreview()).isEqualTo(newPreview);
        assertThat(conversation.getLastMessageAt()).isNotNull();

        verify(conversationRepository, times(1)).save(conversation);
    }

    @Test
    @DisplayName("Should throw exception when conversation not found for update")
    void testUpdateLastMessage_NotFound() {
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> conversationService.updateLastMessage(conversationId, "Preview"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Conversation not found");

        verify(conversationRepository, never()).save(any(Conversation.class));
    }

    @Test
    @DisplayName("Should deactivate conversation successfully")
    void testDeactivateConversation_Success() {
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(conversationRepository.save(any(Conversation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        conversationService.deactivateConversation(conversationId);

        assertThat(conversation.getIsActive()).isFalse();

        verify(conversationRepository, times(1)).save(conversation);
    }

    @Test
    @DisplayName("Should add participant successfully")
    void testAddParticipant_Success() {
        UUID newUserId = UUID.randomUUID();
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(userRepository.existsById(newUserId)).thenReturn(true);
        when(conversationRepository.save(any(Conversation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        conversationService.addParticipant(conversationId, newUserId);

        assertThat(conversation.getParticipantIds()).contains(newUserId);

        verify(conversationRepository, times(1)).save(conversation);
    }

    @Test
    @DisplayName("Should throw exception when adding non-existent user")
    void testAddParticipant_UserNotFound() {
        UUID newUserId = UUID.randomUUID();
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(userRepository.existsById(newUserId)).thenReturn(false);

        assertThatThrownBy(() -> conversationService.addParticipant(conversationId, newUserId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");

        verify(conversationRepository, never()).save(any(Conversation.class));
    }

    @Test
    @DisplayName("Should throw exception when user already participant")
    void testAddParticipant_AlreadyParticipant() {
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(userRepository.existsById(freelancerId)).thenReturn(true);

        assertThatThrownBy(() -> conversationService.addParticipant(conversationId, freelancerId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("User is already a participant");

        verify(conversationRepository, never()).save(any(Conversation.class));
    }

    @Test
    @DisplayName("Should remove participant successfully")
    void testRemoveParticipant_Success() {
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(conversationRepository.save(any(Conversation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        conversationService.removeParticipant(conversationId, freelancerId);

        assertThat(conversation.getParticipantIds()).doesNotContain(freelancerId);

        verify(conversationRepository, times(1)).save(conversation);
    }

    @Test
    @DisplayName("Should deactivate conversation when removing last participant")
    void testRemoveParticipant_LastParticipant() {
        Conversation singleParticipantConv = Conversation.builder()
                .id(conversationId)
                .projectId(projectId)
                .participantIds(new ArrayList<>(List.of(clientId)))
                .isActive(true)
                .build();

        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(singleParticipantConv));
        when(conversationRepository.save(any(Conversation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        conversationService.removeParticipant(conversationId, clientId);

        assertThat(singleParticipantConv.getParticipantIds()).isEmpty();
        assertThat(singleParticipantConv.getIsActive()).isFalse();

        verify(conversationRepository, times(1)).save(singleParticipantConv);
    }

    @Test
    @DisplayName("Should throw exception when removing non-participant")
    void testRemoveParticipant_NotParticipant() {
        UUID nonParticipantId = UUID.randomUUID();
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));

        assertThatThrownBy(() -> conversationService.removeParticipant(conversationId, nonParticipantId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("User is not a participant");

        verify(conversationRepository, never()).save(any(Conversation.class));
    }
}
