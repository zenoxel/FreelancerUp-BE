package com.FreelancerUp;

import com.FreelancerUp.exception.BadRequestException;
import com.FreelancerUp.exception.ResourceNotFoundException;
import com.FreelancerUp.feature.chat.dto.request.SendMessageRequest;
import com.FreelancerUp.feature.chat.dto.response.MessageResponse;
import com.FreelancerUp.feature.chat.repository.MessageRepository;
import com.FreelancerUp.feature.chat.service.impl.MessageServiceImpl;
import com.FreelancerUp.feature.user.repository.UserRepository;
import com.FreelancerUp.model.document.Message;
import com.FreelancerUp.model.entity.User;
import com.FreelancerUp.model.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MessageService.
 *
 * Tests the message lifecycle:
 * 1. Send message to conversation
 * 2. Retrieve conversation messages with pagination
 * 3. Mark message as read
 * 4. Mark all messages as read
 * 5. Get unread count
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Message Service Tests")
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MessageServiceImpl messageService;

    private UUID fromUserId;
    private UUID toUserId;
    private UUID conversationId;
    private String messageId;
    private User fromUser;
    private User toUser;
    private Message message;

    @BeforeEach
    void setUp() {
        fromUserId = UUID.randomUUID();
        toUserId = UUID.randomUUID();
        conversationId = UUID.randomUUID();
        messageId = "507f1f77bcf86cd799439011";

        // Setup users
        fromUser = User.builder()
                .id(fromUserId)
                .email("sender@example.com")
                .fullName("Sender User")
                .role(Role.FREELANCER)
                .avatarUrl("https://example.com/avatar1.jpg")
                .build();

        toUser = User.builder()
                .id(toUserId)
                .email("recipient@example.com")
                .fullName("Recipient User")
                .role(Role.CLIENT)
                .build();

        // Setup message
        message = Message.builder()
                .id(messageId)
                .conversationId(conversationId.toString())
                .fromUserId(fromUserId.toString())
                .toUserId(toUserId.toString())
                .content("Hello, this is a test message")
                .isRead(false)
                .readAt(null)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should send message successfully")
    void testSendMessage_Success() {
        SendMessageRequest request = SendMessageRequest.builder()
                .conversationId(conversationId)
                .content("Hello, this is a test message")
                .toUserId(toUserId)
                .build();

        when(userRepository.findById(fromUserId)).thenReturn(Optional.of(fromUser));
        when(userRepository.findById(toUserId)).thenReturn(Optional.of(toUser));
        when(messageRepository.save(any(Message.class))).thenReturn(message);

        MessageResponse response = messageService.sendMessage(conversationId, fromUserId, request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(messageId);
        assertThat(response.getContent()).isEqualTo("Hello, this is a test message");
        assertThat(response.getFromUserId()).isEqualTo(fromUserId.toString());
        assertThat(response.getToUserId()).isEqualTo(toUserId.toString());
        assertThat(response.getIsRead()).isFalse();

        verify(messageRepository, times(1)).save(any(Message.class));
    }

    @Test
    @DisplayName("Should throw exception when sender not found")
    void testSendMessage_SenderNotFound() {
        SendMessageRequest request = SendMessageRequest.builder()
                .conversationId(conversationId)
                .content("Test message")
                .build();

        when(userRepository.findById(fromUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.sendMessage(conversationId, fromUserId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Sender not found");

        verify(messageRepository, never()).save(any(Message.class));
    }

    @Test
    @DisplayName("Should throw exception when recipient not found")
    void testSendMessage_RecipientNotFound() {
        SendMessageRequest request = SendMessageRequest.builder()
                .conversationId(conversationId)
                .content("Test message")
                .toUserId(toUserId)
                .build();

        when(userRepository.findById(fromUserId)).thenReturn(Optional.of(fromUser));
        when(userRepository.findById(toUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.sendMessage(conversationId, fromUserId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Recipient not found");

        verify(messageRepository, never()).save(any(Message.class));
    }

    @Test
    @DisplayName("Should get conversation messages with pagination")
    void testGetConversationMessages_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Message> messages = List.of(message);
        Page<Message> messagePage = new PageImpl<>(messages, pageable, 1);

        when(userRepository.existsById(fromUserId)).thenReturn(true);
        when(messageRepository.findByConversationIdOrderByCreatedAtDesc(
                conversationId.toString(), pageable))
                .thenReturn(messagePage);
        when(userRepository.findById(fromUserId)).thenReturn(Optional.of(fromUser));

        Page<MessageResponse> response = messageService.getConversationMessages(
                conversationId,
                fromUserId,
                pageable
        );

        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getId()).isEqualTo(messageId);
        assertThat(response.getContent().get(0).getFromUserName()).isEqualTo("Sender User");

        verify(messageRepository, times(1))
                .findByConversationIdOrderByCreatedAtDesc(conversationId.toString(), pageable);
    }

    @Test
    @DisplayName("Should throw exception when user not found for get messages")
    void testGetConversationMessages_UserNotFound() {
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.existsById(fromUserId)).thenReturn(false);

        assertThatThrownBy(() -> messageService.getConversationMessages(conversationId, fromUserId, pageable))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");

        verify(messageRepository, never())
                .findByConversationIdOrderByCreatedAtDesc(any(), any());
    }

    @Test
    @DisplayName("Should get message by ID successfully")
    void testGetMessageById_Success() {
        when(messageRepository.findById(messageId)).thenReturn(Optional.of(message));
        when(userRepository.findById(fromUserId)).thenReturn(Optional.of(fromUser));

        MessageResponse response = messageService.getMessageById(messageId);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(messageId);
        assertThat(response.getContent()).isEqualTo("Hello, this is a test message");
        assertThat(response.getFromUserName()).isEqualTo("Sender User");

        verify(messageRepository, times(1)).findById(messageId);
    }

    @Test
    @DisplayName("Should throw exception when message not found")
    void testGetMessageById_NotFound() {
        when(messageRepository.findById(messageId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.getMessageById(messageId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Message not found");
    }

    @Test
    @DisplayName("Should mark message as read successfully")
    void testMarkAsRead_Success() {
        when(messageRepository.findById(messageId)).thenReturn(Optional.of(message));
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> {
            Message msg = invocation.getArgument(0);
            msg.setIsRead(true);
            msg.setReadAt(LocalDateTime.now());
            return msg;
        });

        MessageResponse response = messageService.markAsRead(messageId, toUserId);

        assertThat(response).isNotNull();
        assertThat(response.getIsRead()).isTrue();
        assertThat(response.getReadAt()).isNotNull();

        verify(messageRepository, times(1)).save(any(Message.class));
    }

    @Test
    @DisplayName("Should throw exception when non-recipient tries to mark as read")
    void testMarkAsRead_NotRecipient() {
        when(messageRepository.findById(messageId)).thenReturn(Optional.of(message));

        UUID wrongUserId = UUID.randomUUID();
        assertThatThrownBy(() -> messageService.markAsRead(messageId, wrongUserId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("You can only mark your own messages as read");

        verify(messageRepository, never()).save(any(Message.class));
    }

    @Test
    @DisplayName("Should return early when message already read")
    void testMarkAsRead_AlreadyRead() {
        message.setIsRead(true);
        message.setReadAt(LocalDateTime.now());

        when(messageRepository.findById(messageId)).thenReturn(Optional.of(message));

        MessageResponse response = messageService.markAsRead(messageId, toUserId);

        assertThat(response).isNotNull();
        assertThat(response.getIsRead()).isTrue();

        verify(messageRepository, never()).save(any(Message.class));
    }

    @Test
    @DisplayName("Should mark all messages as read successfully")
    void testMarkAllAsRead_Success() {
        Message message2 = Message.builder()
                .id("507f1f77bcf86cd799439012")
                .conversationId(conversationId.toString())
                .fromUserId(fromUserId.toString())
                .toUserId(toUserId.toString())
                .content("Second message")
                .isRead(false)
                .build();

        List<Message> unreadMessages = List.of(message, message2);

        when(messageRepository.findByConversationIdAndToUserIdAndIsReadOrderByCreatedAtDesc(
                conversationId.toString(),
                toUserId.toString(),
                false
        )).thenReturn(unreadMessages);
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0));

        int markedCount = messageService.markAllAsRead(conversationId, toUserId);

        assertThat(markedCount).isEqualTo(2);

        verify(messageRepository, times(2)).save(any(Message.class));
    }

    @Test
    @DisplayName("Should get unread count successfully")
    void testGetUnreadCount_Success() {
        when(messageRepository.countByConversationIdAndToUserIdAndIsRead(
                conversationId.toString(),
                toUserId.toString(),
                false
        )).thenReturn(5L);

        int count = messageService.getUnreadCount(conversationId, toUserId);

        assertThat(count).isEqualTo(5);

        verify(messageRepository, times(1))
                .countByConversationIdAndToUserIdAndIsRead(conversationId.toString(), toUserId.toString(), false);
    }

    @Test
    @DisplayName("Should return zero when no unread messages")
    void testGetUnreadCount_NoUnread() {
        when(messageRepository.countByConversationIdAndToUserIdAndIsRead(
                conversationId.toString(),
                toUserId.toString(),
                false
        )).thenReturn(0L);

        int count = messageService.getUnreadCount(conversationId, toUserId);

        assertThat(count).isEqualTo(0);
    }
}
