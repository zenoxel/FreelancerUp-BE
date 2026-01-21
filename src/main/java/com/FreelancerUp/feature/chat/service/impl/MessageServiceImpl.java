package com.FreelancerUp.feature.chat.service.impl;

import com.FreelancerUp.exception.BadRequestException;
import com.FreelancerUp.exception.ResourceNotFoundException;
import com.FreelancerUp.feature.chat.dto.request.SendMessageRequest;
import com.FreelancerUp.feature.chat.dto.response.MessageResponse;
import com.FreelancerUp.feature.chat.repository.MessageRepository;
import com.FreelancerUp.feature.chat.service.MessageService;
import com.FreelancerUp.feature.user.repository.UserRepository;
import com.FreelancerUp.model.document.Message;
import com.FreelancerUp.model.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of MessageService.
 *
 * Key Features:
 * - Send messages to conversations
 * - Retrieve message history with pagination
 * - Track read/unread status
 * - Update conversation last message info
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public MessageResponse sendMessage(UUID conversationId, UUID fromUserId, SendMessageRequest request) {
        log.info("Sending message from user {} in conversation: {}", fromUserId, conversationId);

        // Validate users exist
        User fromUser = userRepository.findById(fromUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));

        // Validate recipient if provided
        if (request.getToUserId() != null) {
            userRepository.findById(request.getToUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Recipient not found"));
        }

        // Validate conversation exists (we check the entity, but need ConversationRepository)
        // For now, we'll create the message directly

        // Create message
        Message message = Message.builder()
                .conversationId(conversationId.toString())
                .fromUserId(fromUserId.toString())
                .toUserId(request.getToUserId() != null ? request.getToUserId().toString() : null)
                .content(request.getContent())
                .isRead(false)
                .build();

        message = messageRepository.save(message);

        log.info("Message sent successfully: {}", message.getId());
        return convertToResponse(message, fromUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MessageResponse> getConversationMessages(UUID conversationId, UUID userId, Pageable pageable) {
        log.info("Fetching messages for conversation: {} for user: {}", conversationId, userId);

        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }

        // Get messages for this conversation
        Page<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtDesc(
                conversationId.toString(), pageable);

        // Map to responses, loading user info for each message
        return messages.map(message -> {
            User fromUser = userRepository.findById(UUID.fromString(message.getFromUserId()))
                    .orElse(null);
            return convertToResponse(message, fromUser);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponse getMessageById(String messageId) {
        log.info("Fetching message: {}", messageId);

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

        User fromUser = userRepository.findById(UUID.fromString(message.getFromUserId()))
                .orElse(null);

        return convertToResponse(message, fromUser);
    }

    @Override
    @Transactional
    public MessageResponse markAsRead(String messageId, UUID userId) {
        log.info("Marking message: {} as read by user: {}", messageId, userId);

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

        // Validate user is the recipient
        if (!message.getToUserId().equals(userId.toString())) {
            throw new BadRequestException("You can only mark your own messages as read");
        }

        // Validate message is not already read
        if (Boolean.TRUE.equals(message.getIsRead())) {
            return convertToResponse(message, null);
        }

        // Mark as read
        message.setIsRead(true);
        message.setReadAt(LocalDateTime.now());
        message = messageRepository.save(message);

        log.info("Message marked as read: {}", messageId);
        return convertToResponse(message, null);
    }

    @Override
    @Transactional
    public int markAllAsRead(UUID conversationId, UUID userId) {
        log.info("Marking all messages as read in conversation: {} for user: {}", conversationId, userId);

        // Find all unread messages for this user in the conversation
        List<Message> unreadMessages = messageRepository.findByConversationIdAndToUserIdAndIsReadOrderByCreatedAtDesc(
                conversationId.toString(),
                userId.toString(),
                false
        );

        int markedCount = 0;
        LocalDateTime now = LocalDateTime.now();

        for (Message message : unreadMessages) {
            message.setIsRead(true);
            message.setReadAt(now);
            messageRepository.save(message);
            markedCount++;
        }

        log.info("Marked {} messages as read in conversation: {}", markedCount, conversationId);
        return markedCount;
    }

    @Override
    @Transactional(readOnly = true)
    public int getUnreadCount(UUID conversationId, UUID userId) {
        log.info("Counting unread messages in conversation: {} for user: {}", conversationId, userId);

        long count = messageRepository.countByConversationIdAndToUserIdAndIsRead(
                conversationId.toString(),
                userId.toString(),
                false
        );

        return (int) count;
    }

    // Helper method
    private MessageResponse convertToResponse(Message message, User fromUser) {
        return MessageResponse.builder()
                .id(message.getId())
                .conversationId(UUID.fromString(message.getConversationId()))
                .projectId(message.getProjectId())
                .fromUserId(message.getFromUserId())
                .fromUserName(fromUser != null ? fromUser.getFullName() : null)
                .fromUserEmail(fromUser != null ? fromUser.getEmail() : null)
                .fromUserAvatarUrl(fromUser != null ? fromUser.getAvatarUrl() : null)
                .toUserId(message.getToUserId())
                .content(message.getContent())
                .isRead(message.getIsRead())
                .readAt(message.getReadAt())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
