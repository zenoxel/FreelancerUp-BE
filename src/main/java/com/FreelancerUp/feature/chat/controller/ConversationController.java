package com.FreelancerUp.feature.chat.controller;

import com.FreelancerUp.feature.chat.dto.response.ConversationResponse;
import com.FreelancerUp.feature.chat.service.ConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for Conversation operations.
 *
 * Endpoints:
 * - GET /api/v1/conversations/project/{projectId} - Get or create conversation for project
 * - GET /api/v1/conversations/{conversationId} - Get conversation by ID
 * - GET /api/v1/conversations/my - Get current user's conversations
 * - PUT /api/v1/conversations/{conversationId}/deactivate - Deactivate conversation
 * - POST /api/v1/conversations/{conversationId}/participants - Add participant
 * - DELETE /api/v1/conversations/{conversationId}/participants/{userId} - Remove participant
 */
@RestController
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
@Tag(name = "Conversation", description = "Conversation management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ConversationController {

    private final ConversationService conversationService;

    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAnyRole('CLIENT', 'FREELANCER')")
    @Operation(summary = "Get or create conversation for project", description = "Returns existing conversation or creates new one for the project")
    public ResponseEntity<ConversationResponse> getOrCreateConversation(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Project ID (MongoDB)") @PathVariable String projectId) {

        UUID clientId = UUID.fromString(userDetails.getUsername());
        ConversationResponse conversation = conversationService.getOrCreateConversation(projectId, clientId);
        return ResponseEntity.ok(conversation);
    }

    @GetMapping("/{conversationId}")
    @PreAuthorize("hasAnyRole('CLIENT', 'FREELANCER')")
    @Operation(summary = "Get conversation by ID", description = "Returns conversation information by ID")
    public ResponseEntity<ConversationResponse> getConversationById(
            @Parameter(description = "Conversation ID") @PathVariable UUID conversationId) {

        ConversationResponse conversation = conversationService.getConversationById(conversationId);
        return ResponseEntity.ok(conversation);
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('CLIENT', 'FREELANCER')")
    @Operation(summary = "Get current user's conversations", description = "Returns all conversations for the authenticated user")
    public ResponseEntity<List<ConversationResponse>> getMyConversations(
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = UUID.fromString(userDetails.getUsername());
        List<ConversationResponse> conversations = conversationService.getConversationsForUser(userId);
        return ResponseEntity.ok(conversations);
    }

    @GetMapping("/my/paginated")
    @PreAuthorize("hasAnyRole('CLIENT', 'FREELANCER')")
    @Operation(summary = "Get current user's conversations (paginated)", description = "Returns paginated conversations for the authenticated user")
    public ResponseEntity<Page<ConversationResponse>> getMyConversationsPaginated(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "DESC") String sortDirection) {

        UUID userId = UUID.fromString(userDetails.getUsername());
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), "lastMessageAt");
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ConversationResponse> conversations = conversationService.getConversationsForUserPaginated(
                userId,
                pageable
        );
        return ResponseEntity.ok(conversations);
    }

    @PutMapping("/{conversationId}/deactivate")
    @PreAuthorize("hasAnyRole('CLIENT', 'FREELANCER')")
    @Operation(summary = "Deactivate conversation", description = "Deactivates a conversation (soft delete)")
    public ResponseEntity<Void> deactivateConversation(
            @Parameter(description = "Conversation ID") @PathVariable UUID conversationId) {

        conversationService.deactivateConversation(conversationId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{conversationId}/participants")
    @PreAuthorize("hasAnyRole('CLIENT', 'FREELANCER')")
    @Operation(summary = "Add participant to conversation", description = "Adds a user as a participant to the conversation")
    public ResponseEntity<Void> addParticipant(
            @Parameter(description = "Conversation ID") @PathVariable UUID conversationId,
            @Parameter(description = "User ID to add") @RequestParam UUID userId) {

        conversationService.addParticipant(conversationId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{conversationId}/participants/{userId}")
    @PreAuthorize("hasAnyRole('CLIENT', 'FREELANCER')")
    @Operation(summary = "Remove participant from conversation", description = "Removes a user from the conversation")
    public ResponseEntity<Void> removeParticipant(
            @Parameter(description = "Conversation ID") @PathVariable UUID conversationId,
            @Parameter(description = "User ID to remove") @PathVariable UUID userId) {

        conversationService.removeParticipant(conversationId, userId);
        return ResponseEntity.ok().build();
    }
}
