package com.FreelancerUp.feature.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for conversation information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Conversation information response")
public class ConversationResponse {

    @Schema(description = "Conversation ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Project ID", example = "507f1f77bcf86cd799439011")
    private String projectId;

    @Schema(description = "Project title", example = "Web Development Project")
    private String projectTitle;

    @Schema(description = "List of participant user IDs", example = "[\"uuid1\", \"uuid2\"]")
    private java.util.List<UUID> participantIds;

    @Schema(description = "Last message in conversation", example = "Sounds good, let's start!")
    private String lastMessagePreview;

    @Schema(description="Timestamp of last message")
    private LocalDateTime lastMessageAt;

    @Schema(description = "Conversation active status", example = "true")
    private Boolean isActive;

    @Schema(description = "Unread message count for current user")
    private Integer unreadCount;

    @Schema(description = "Conversation creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}
