package com.FreelancerUp.feature.review.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for review information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Review information response")
public class ReviewResponse {

    @Schema(description = "Review ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Project ID (MongoDB)", example = "507f1f77bcf86cd799439011")
    private String projectId;

    @Schema(description = "Project title", example = "Web Development Project")
    private String projectTitle;

    @Schema(description = "Reviewer User ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID fromUserId;

    @Schema(description = "Reviewer full name", example = "John Doe")
    private String fromUserName;

    @Schema(description = "Reviewer avatar URL", example = "https://example.com/avatar.jpg")
    private String fromUserAvatarUrl;

    @Schema(description = "Reviewed User ID", example = "650e8400-e29b-41d4-a716-446655440001")
    private UUID toUserId;

    @Schema(description = "Reviewed user full name", example = "Jane Smith")
    private String toUserName;

    @Schema(description = "Overall rating (1-5)", example = "5")
    private Integer rating;

    @Schema(description = "Review comment", example = "Excellent work, delivered on time!")
    private String comment;

    @Schema(description = "Communication rating (1-5)", example = "5")
    private Integer communicationRating;

    @Schema(description = "Quality rating (1-5)", example = "5")
    private Integer qualityRating;

    @Schema(description = "Timeline rating (1-5)", example = "4")
    private Integer timelineRating;

    @Schema(description = "Professionalism rating (1-5)", example = "5")
    private Integer professionalismRating;

    @Schema(description = "Responsiveness rating (1-5)", example = "5")
    private Integer responsivenessRating;

    @Schema(description = "Visibility status", example = "true")
    private Boolean isVisible;

    @Schema(description = "Review creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Review update timestamp")
    private LocalDateTime updatedAt;
}
