package com.FreelancerUp.feature.chat.service;

import com.FreelancerUp.feature.chat.dto.response.ConversationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for Conversation operations.
 * Handles conversation creation, retrieval, and management.
 */
public interface ConversationService {

    /**
     * Get or create a conversation for a project.
     * If a conversation already exists for the project, returns it.
     * Otherwise, creates a new one with the client and all bidders as participants.
     *
     * @param projectId Project ID (MongoDB)
     * @param clientId Client User ID
     * @return ConversationResponse
     */
    ConversationResponse getOrCreateConversation(String projectId, UUID clientId);

    /**
     * Get conversation by ID.
     *
     * @param conversationId Conversation ID
     * @return ConversationResponse
     */
    ConversationResponse getConversationById(UUID conversationId);

    /**
     * Get conversation by project ID.
     *
     * @param projectId Project ID (MongoDB)
     * @return ConversationResponse
     */
    ConversationResponse getConversationByProjectId(String projectId);

    /**
     * Get all active conversations for a user (as participant).
     *
     * @param userId User ID
     * @return List of ConversationResponse
     */
    List<ConversationResponse> getConversationsForUser(UUID userId);

    /**
     * Get paginated conversations for a user.
     *
     * @param userId User ID
     * @param pageable Pageable parameters
     * @return Page of ConversationResponse
     */
    Page<ConversationResponse> getConversationsForUserPaginated(UUID userId, Pageable pageable);

    /**
     * Update conversation's last message info.
     * Called when a new message is sent.
     *
     * @param conversationId Conversation ID
     * @param lastMessagePreview Preview of the last message
     */
    void updateLastMessage(UUID conversationId, String lastMessagePreview);

    /**
     * Deactivate a conversation (soft delete).
     *
     * @param conversationId Conversation ID
     */
    void deactivateConversation(UUID conversationId);

    /**
     * Add a participant to an existing conversation.
     *
     * @param conversationId Conversation ID
     * @param userId User ID to add
     */
    void addParticipant(UUID conversationId, UUID userId);

    /**
     * Remove a participant from a conversation.
     *
     * @param conversationId Conversation ID
     * @param userId User ID to remove
     */
    void removeParticipant(UUID conversationId, UUID userId);
}
