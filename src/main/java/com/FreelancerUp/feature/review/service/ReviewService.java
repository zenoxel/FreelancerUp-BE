package com.FreelancerUp.feature.review.service;

import com.FreelancerUp.feature.review.dto.request.CreateReviewRequest;
import com.FreelancerUp.feature.review.dto.response.ReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for Review operations.
 * Handles review creation, retrieval, and reputation calculation.
 */
public interface ReviewService {

    /**
     * Create a new review for a completed project.
     * Validates that the project is completed and no duplicate review exists.
     *
     * @param fromUserId User ID of the reviewer
     * @param request Review creation request
     * @return ReviewResponse
     */
    ReviewResponse createReview(UUID fromUserId, CreateReviewRequest request);

    /**
     * Get review by ID.
     *
     * @param reviewId Review ID
     * @return ReviewResponse
     */
    ReviewResponse getReviewById(UUID reviewId);

    /**
     * Get all reviews for a project.
     *
     * @param projectId Project ID (MongoDB)
     * @return List of ReviewResponse
     */
    List<ReviewResponse> getReviewsByProject(String projectId);

    /**
     * Get paginated reviews for a project.
     *
     * @param projectId Project ID (MongoDB)
     * @param pageable Pageable parameters
     * @return Page of ReviewResponse
     */
    Page<ReviewResponse> getReviewsByProjectPaginated(String projectId, Pageable pageable);

    /**
     * Get all reviews for a user (reviews received).
     *
     * @param userId User ID
     * @return List of ReviewResponse
     */
    List<ReviewResponse> getReviewsByUser(UUID userId);

    /**
     * Get paginated reviews for a user.
     *
     * @param userId User ID
     * @param pageable Pageable parameters
     * @return Page of ReviewResponse
     */
    Page<ReviewResponse> getReviewsByUserPaginated(UUID userId, Pageable pageable);

    /**
     * Get all reviews given by a user.
     *
     * @param userId User ID
     * @return List of ReviewResponse
     */
    List<ReviewResponse> getReviewsGivenByUser(UUID userId);

    /**
     * Calculate reputation score for a user.
     * Includes average rating, total reviews, and category breakdowns.
     *
     * @param userId User ID
     * @return Reputation data (average rating, total count)
     */
    ReputationData getUserReputation(UUID userId);

    /**
     * Update review visibility (admin function).
     *
     * @param reviewId Review ID
     * @param isVisible Visibility status
     */
    void updateReviewVisibility(UUID reviewId, boolean isVisible);

    /**
     * Data class for reputation information.
     */
    record ReputationData(
            Double averageRating,
            Long totalReviews,
            Integer communicationRating,
            Integer qualityRating,
            Integer timelineRating,
            Integer professionalismRating,
            Integer responsivenessRating
    ) {}
}
