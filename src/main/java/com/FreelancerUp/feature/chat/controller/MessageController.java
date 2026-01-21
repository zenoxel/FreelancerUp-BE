package com.FreelancerUp.feature.chat.controller;

import com.FreelancerUp.feature.chat.dto.request.SendMessageRequest;
import com.FreelancerUp.feature.chat.dto.response.MessageResponse;
import com.FreelancerUp.feature.chat.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller for Message operations.
 *
 * Endpoints:
 * - POST /api/v1/messages/send - Send a message to a conversation
 * - GET /api/v1/messages/conversation/{conversationId} - Get messages for a conversation
 * - GET /api/v1/messages/{messageId} - Get message by ID
 * - PUT /api/v1/messages/{messageId}/read - Mark message as read
 * - PUT /api/v1/messages/conversation/{conversationId}/read-all - Mark all messages as read
 * - GET /api/v1/messages/conversation/{conversationId}/unread-count - Get unread message count
 */
@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
@Tag(name = "Message", description = "Message management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class MessageController {

    private final MessageService messageService;

    @PostMapping("/send")
    @PreAuthorize("hasAnyRole('CLIENT', 'FREELANCER')")
    @Operation(summary = "Send a message", description = "Send a message to a conversation")
    public ResponseEntity<MessageResponse> sendMessage(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody SendMessageRequest request) {

        UUID fromUserId = UUID.fromString(userDetails.getUsername());
        MessageResponse message = messageService.sendMessage(
                request.getConversationId(),
                fromUserId,
                request
        );
        return ResponseEntity.ok(message);
    }

    @GetMapping("/conversation/{conversationId}")
    @PreAuthorize("hasAnyRole('CLIENT', 'FREELANCER')")
    @Operation(summary = "Get conversation messages", description = "Get paginated messages for a conversation")
    public ResponseEntity<Page<MessageResponse>> getConversationMessages(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Conversation ID") @PathVariable UUID conversationId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "50") int size,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "DESC") String sortDirection) {

        UUID userId = UUID.fromString(userDetails.getUsername());
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), "createdAt");
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<MessageResponse> messages = messageService.getConversationMessages(
                conversationId,
                userId,
                pageable
        );
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/{messageId}")
    @PreAuthorize("hasAnyRole('CLIENT', 'FREELANCER')")
    @Operation(summary = "Get message by ID", description = "Returns message information by ID")
    public ResponseEntity<MessageResponse> getMessageById(
            @Parameter(description = "Message ID (MongoDB)") @PathVariable String messageId) {

        MessageResponse message = messageService.getMessageById(messageId);
        return ResponseEntity.ok(message);
    }

    @PutMapping("/{messageId}/read")
    @PreAuthorize("hasAnyRole('CLIENT', 'FREELANCER')")
    @Operation(summary = "Mark message as read", description = "Mark a specific message as read")
    public ResponseEntity<MessageResponse> markAsRead(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Message ID (MongoDB)") @PathVariable String messageId) {

        UUID userId = UUID.fromString(userDetails.getUsername());
        MessageResponse message = messageService.markAsRead(messageId, userId);
        return ResponseEntity.ok(message);
    }

    @PutMapping("/conversation/{conversationId}/read-all")
    @PreAuthorize("hasAnyRole('CLIENT', 'FREELANCER')")
    @Operation(summary = "Mark all messages as read", description = "Mark all unread messages in a conversation as read")
    public ResponseEntity<Integer> markAllAsRead(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Conversation ID") @PathVariable UUID conversationId) {

        UUID userId = UUID.fromString(userDetails.getUsername());
        int markedCount = messageService.markAllAsRead(conversationId, userId);
        return ResponseEntity.ok(markedCount);
    }

    @GetMapping("/conversation/{conversationId}/unread-count")
    @PreAuthorize("hasAnyRole('CLIENT', 'FREELANCER')")
    @Operation(summary = "Get unread message count", description = "Get count of unread messages in a conversation")
    public ResponseEntity<Integer> getUnreadCount(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Conversation ID") @PathVariable UUID conversationId) {

        UUID userId = UUID.fromString(userDetails.getUsername());
        int count = messageService.getUnreadCount(conversationId, userId);
        return ResponseEntity.ok(count);
    }
}
