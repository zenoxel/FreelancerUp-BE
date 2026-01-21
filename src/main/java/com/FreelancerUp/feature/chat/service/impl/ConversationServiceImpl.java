package com.FreelancerUp.feature.chat.service.impl;

import com.FreelancerUp.exception.BadRequestException;
import com.FreelancerUp.exception.ResourceNotFoundException;
import com.FreelancerUp.feature.chat.dto.response.ConversationResponse;
import com.FreelancerUp.feature.chat.repository.ConversationRepository;
import com.FreelancerUp.feature.chat.service.ConversationService;
import com.FreelancerUp.feature.project.repository.ProjectRepository;
import com.FreelancerUp.feature.user.repository.UserRepository;
import com.FreelancerUp.model.document.Project;
import com.FreelancerUp.model.entity.Conversation;
import com.FreelancerUp.model.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of ConversationService.
 *
 * Key Features:
 * - Get or create conversations for projects
 * - Retrieve conversations with pagination
 * - Manage conversation participants
 * - Track last message info
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    @Override
    @Transactional
    public ConversationResponse getOrCreateConversation(String projectId, UUID clientId) {
        log.info("Getting or creating conversation for project: {}", projectId);

        // Check if conversation already exists
        return conversationRepository.findByProjectId(projectId)
                .map(this::convertToResponse)
                .orElseGet(() -> createNewConversation(projectId, clientId));
    }

    @Override
    @Transactional(readOnly = true)
    public ConversationResponse getConversationById(UUID conversationId) {
        log.info("Fetching conversation: {}", conversationId);

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        return convertToResponse(conversation);
    }

    @Override
    @Transactional(readOnly = true)
    public ConversationResponse getConversationByProjectId(String projectId) {
        log.info("Fetching conversation for project: {}", projectId);

        Conversation conversation = conversationRepository.findByProjectId(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found for this project"));

        return convertToResponse(conversation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationResponse> getConversationsForUser(UUID userId) {
        log.info("Fetching conversations for user: {}", userId);

        List<Conversation> conversations = conversationRepository.findActiveConversationsForUser(userId);

        return conversations.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConversationResponse> getConversationsForUserPaginated(UUID userId, Pageable pageable) {
        log.info("Fetching paginated conversations for user: {}", userId);

        // For simplicity, we'll use the non-paginated query and convert to Page
        // In production, you'd want a proper paginated query in the repository
        List<Conversation> conversations = conversationRepository.findActiveConversationsForUser(userId);

        // Convert to page manually
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), conversations.size());

        List<Conversation> pagedConversations = conversations.subList(
                Math.min(start, conversations.size()),
                Math.min(end, conversations.size())
        );

        return new org.springframework.data.domain.PageImpl<>(
                pagedConversations.stream()
                        .map(this::convertToResponse)
                        .collect(Collectors.toList()),
                pageable,
                conversations.size()
        );
    }

    @Override
    @Transactional
    public void updateLastMessage(UUID conversationId, String lastMessagePreview) {
        log.info("Updating last message for conversation: {}", conversationId);

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        conversation.setLastMessageAt(LocalDateTime.now());
        conversation.setLastMessagePreview(lastMessagePreview);
        conversationRepository.save(conversation);
    }

    @Override
    @Transactional
    public void deactivateConversation(UUID conversationId) {
        log.info("Deactivating conversation: {}", conversationId);

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        conversation.setIsActive(false);
        conversationRepository.save(conversation);
    }

    @Override
    @Transactional
    public void addParticipant(UUID conversationId, UUID userId) {
        log.info("Adding participant {} to conversation: {}", userId, conversationId);

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }

        // Check if user is already a participant
        if (conversation.getParticipantIds().contains(userId)) {
            throw new BadRequestException("User is already a participant");
        }

        conversation.getParticipantIds().add(userId);
        conversationRepository.save(conversation);
    }

    @Override
    @Transactional
    public void removeParticipant(UUID conversationId, UUID userId) {
        log.info("Removing participant {} from conversation: {}", userId, conversationId);

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        if (!conversation.getParticipantIds().contains(userId)) {
            throw new BadRequestException("User is not a participant");
        }

        conversation.getParticipantIds().remove(userId);

        // Deactivate if no participants left
        if (conversation.getParticipantIds().isEmpty()) {
            conversation.setIsActive(false);
        }

        conversationRepository.save(conversation);
    }

    /**
     * Create a new conversation for a project.
     */
    private ConversationResponse createNewConversation(String projectId, UUID clientId) {
        log.info("Creating new conversation for project: {}", projectId);

        // Validate project exists
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        // Validate client exists
        userRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

        // Create conversation with client as initial participant
        Conversation conversation = Conversation.builder()
                .projectId(projectId)
                .participantIds(new ArrayList<>(List.of(clientId)))
                .lastMessageAt(null)
                .lastMessagePreview(null)
                .isActive(true)
                .build();

        conversation = conversationRepository.save(conversation);

        log.info("Conversation created: {}", conversation.getId());
        return convertToResponse(conversation, project.getTitle());
    }

    /**
     * Convert Conversation entity to ConversationResponse.
     */
    private ConversationResponse convertToResponse(Conversation conversation) {
        // Try to fetch project title
        String projectTitle = null;
        try {
            Project project = projectRepository.findById(conversation.getProjectId()).orElse(null);
            projectTitle = project != null ? project.getTitle() : null;
        } catch (Exception e) {
            log.warn("Failed to fetch project title for conversation: {}", conversation.getId());
        }

        return convertToResponse(conversation, projectTitle);
    }

    /**
     * Convert Conversation entity to ConversationResponse with project title.
     */
    private ConversationResponse convertToResponse(Conversation conversation, String projectTitle) {
        return ConversationResponse.builder()
                .id(conversation.getId())
                .projectId(conversation.getProjectId())
                .projectTitle(projectTitle)
                .participantIds(conversation.getParticipantIds())
                .lastMessagePreview(conversation.getLastMessagePreview())
                .lastMessageAt(conversation.getLastMessageAt())
                .isActive(conversation.getIsActive())
                .unreadCount(0) // Calculated separately based on user
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .build();
    }
}
