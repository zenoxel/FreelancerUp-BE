package com.FreelancerUp.feature.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for message information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Message information response")
public class MessageResponse {

    @Schema(description = "Message ID (MongoDB)", example = "507f1f77bcf86cd799439011")
    private String id;

    @Schema(description = "Conversation ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID conversationId;

    @Schema(description = "Project ID", example = "507f1f77bcf86cd799439011")
    private String projectId;

    @Schema(description = "Sender User ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private String fromUserId;

    @Schema(description = "Sender full name", example = "John Doe")
    private String fromUserName;

    @Schema(description = "Sender email", example = "john@example.com")
    private String fromUserEmail;

    @Schema(description = "Sender avatar URL", example = "https://example.com/avatar.jpg")
    private String fromUserAvatarUrl;

    @Schema(description = "Recipient User ID", example = "650e8400-e29b-41d4-a716-446655440001")
    private String toUserId;

    @Schema(description = "Message content", example = "When can you start working on this project?")
    private String content;

    @Schema(description = "Read status", example = "false")
    private Boolean isRead;

    @Schema(description = "Timestamp when message was read")
    private LocalDateTime readAt;

    @Schema(description = "Message creation timestamp")
    private LocalDateTime createdAt;
}
