package com.FreelancerUp.feature.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for sending a message.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to send a message")
public class SendMessageRequest {

    @NotNull(message = "Conversation ID is required")
    @Schema(description = "Conversation ID (UUID from PostgreSQL)", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID conversationId;

    @NotBlank(message = "Message content is required")
    @Schema(description = "Message content", example = "When can you start working on this project?")
    private String content;

    @Schema(description = "Recipient User ID (optional for validation)", example = "650e8400-e29b-41d4-a716-446655440001")
    private UUID toUserId;
}
