package com.FreelancerUp.feature.chat.service;

import com.FreelancerUp.feature.chat.dto.request.SendMessageRequest;
import com.FreelancerUp.feature.chat.dto.response.MessageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for Message operations.
 * Handles sending, retrieving, and read status tracking for messages.
 */
public interface MessageService {

    /**
     * Send a message to a conversation.
     *
     * @param conversationId Conversation ID
     * @param fromUserId Sender user ID
     * @param request Message request containing content and recipient
     * @return MessageResponse
     */
    MessageResponse sendMessage(UUID conversationId, UUID fromUserId, SendMessageRequest request);

    /**
     * Get messages for a conversation with pagination.
     *
     * @param conversationId Conversation ID
     * @param userId Current user ID (for validation)
     * @param pageable Pageable
     * @return Page of MessageResponse
     */
    Page<MessageResponse> getConversationMessages(UUID conversationId, UUID userId, Pageable pageable);

    /**
     * Get a message by ID.
     *
     * @param messageId Message ID (MongoDB)
     * @return MessageResponse
     */
    MessageResponse getMessageById(String messageId);

    /**
     * Mark a message as read.
     *
     * @param messageId Message ID (MongoDB)
     * @param userId User ID marking as read
     * @return MessageResponse
     */
    MessageResponse markAsRead(String messageId, UUID userId);

    /**
     * Mark all unread messages in a conversation as read for a user.
     *
     * @param conversationId Conversation ID
     * @param userId User ID
     * @return Number of messages marked as read
     */
    int markAllAsRead(UUID conversationId, UUID userId);

    /**
     * Get unread message count for a user in a conversation.
     *
     * @param conversationId Conversation ID
     * @param userId User ID
     * @return Unread count
     */
    int getUnreadCount(UUID conversationId, UUID userId);
}
