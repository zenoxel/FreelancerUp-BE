package com.FreelancerUp.feature.review.controller;

import com.FreelancerUp.feature.review.dto.request.CreateReviewRequest;
import com.FreelancerUp.feature.review.dto.response.ReviewResponse;
import com.FreelancerUp.feature.review.service.ReviewService;
import com.FreelancerUp.feature.review.service.ReviewService.ReputationData;
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

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for Review operations.
 *
 * Endpoints:
 * - POST /api/v1/reviews - Create a review
 * - GET /api/v1/reviews/{reviewId} - Get review by ID
 * - GET /api/v1/reviews/project/{projectId} - Get reviews for a project
 * - GET /api/v1/reviews/user/{userId} - Get reviews for a user
 * - GET /api/v1/reviews/user/{userId}/reputation - Get user reputation data
 * - GET /api/v1/reviews/given-by-me - Get reviews given by current user
 * - PATCH /api/v1/reviews/{reviewId}/visibility - Update review visibility (ADMIN)
 */
@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "Review", description = "Review and reputation management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENT', 'FREELANCER')")
    @Operation(summary = "Create a review", description = "Create a review for a completed project")
    public ResponseEntity<ReviewResponse> createReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateReviewRequest request) {

        UUID fromUserId = UUID.fromString(userDetails.getUsername());
        ReviewResponse review = reviewService.createReview(fromUserId, request);
        return ResponseEntity.ok(review);
    }

    @GetMapping("/{reviewId}")
    @Operation(summary = "Get review by ID", description = "Returns review information by ID")
    public ResponseEntity<ReviewResponse> getReviewById(
            @Parameter(description = "Review ID") @PathVariable UUID reviewId) {

        ReviewResponse review = reviewService.getReviewById(reviewId);
        return ResponseEntity.ok(review);
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "Get reviews for a project", description = "Returns all reviews for a specific project")
    public ResponseEntity<List<ReviewResponse>> getReviewsByProject(
            @Parameter(description = "Project ID (MongoDB)") @PathVariable String projectId) {

        List<ReviewResponse> reviews = reviewService.getReviewsByProject(projectId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/project/{projectId}/paginated")
    @Operation(summary = "Get paginated reviews for a project", description = "Returns paginated reviews for a specific project")
    public ResponseEntity<Page<ReviewResponse>> getReviewsByProjectPaginated(
            @Parameter(description = "Project ID (MongoDB)") @PathVariable String projectId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "DESC") String sortDirection) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), "createdAt");
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ReviewResponse> reviews = reviewService.getReviewsByProjectPaginated(projectId, pageable);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get reviews for a user", description = "Returns all reviews received by a user")
    public ResponseEntity<List<ReviewResponse>> getReviewsByUser(
            @Parameter(description = "User ID") @PathVariable UUID userId) {

        List<ReviewResponse> reviews = reviewService.getReviewsByUser(userId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/user/{userId}/paginated")
    @Operation(summary = "Get paginated reviews for a user", description = "Returns paginated reviews received by a user")
    public ResponseEntity<Page<ReviewResponse>> getReviewsByUserPaginated(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "DESC") String sortDirection) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), "createdAt");
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ReviewResponse> reviews = reviewService.getReviewsByUserPaginated(userId, pageable);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/user/{userId}/reputation")
    @Operation(summary = "Get user reputation data", description = "Returns reputation score and statistics for a user")
    public ResponseEntity<ReputationData> getUserReputation(
            @Parameter(description = "User ID") @PathVariable UUID userId) {

        ReputationData reputation = reviewService.getUserReputation(userId);
        return ResponseEntity.ok(reputation);
    }

    @GetMapping("/given-by-me")
    @PreAuthorize("hasAnyRole('CLIENT', 'FREELANCER')")
    @Operation(summary = "Get reviews given by current user", description = "Returns all reviews given by the authenticated user")
    public ResponseEntity<List<ReviewResponse>> getReviewsGivenByMe(
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = UUID.fromString(userDetails.getUsername());
        List<ReviewResponse> reviews = reviewService.getReviewsGivenByUser(userId);
        return ResponseEntity.ok(reviews);
    }

    @PatchMapping("/{reviewId}/visibility")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update review visibility", description = "Update the visibility status of a review (admin only)")
    public ResponseEntity<Void> updateReviewVisibility(
            @Parameter(description = "Review ID") @PathVariable UUID reviewId,
            @Parameter(description = "Visibility status") @RequestParam boolean isVisible) {

        reviewService.updateReviewVisibility(reviewId, isVisible);
        return ResponseEntity.ok().build();
    }
}
