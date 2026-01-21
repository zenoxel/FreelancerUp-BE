package com.FreelancerUp.feature.review.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a review.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a review")
public class CreateReviewRequest {

    @NotNull(message = "Project ID is required")
    @Schema(description = "Project ID (MongoDB)", example = "507f1f77bcf86cd799439011")
    private String projectId;

    @NotNull(message = "Recipient User ID is required")
    @Schema(description = "User ID being reviewed", example = "550e8400-e29b-41d4-a716-446655440001")
    private java.util.UUID toUserId;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    @Schema(description = "Overall rating (1-5)", example = "5")
    private Integer rating;

    @Schema(description = "Review comment", example = "Excellent work, delivered on time!")
    private String comment;

    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    @Schema(description = "Communication rating (1-5)", example = "5")
    private Integer communicationRating;

    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    @Schema(description = "Quality rating (1-5)", example = "5")
    private Integer qualityRating;

    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    @Schema(description = "Timeline rating (1-5)", example = "4")
    private Integer timelineRating;

    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    @Schema(description = "Professionalism rating (1-5)", example = "5")
    private Integer professionalismRating;

    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    @Schema(description = "Responsiveness rating (1-5)", example = "5")
    private Integer responsivenessRating;
}
