package com.FreelancerUp.feature.review.service.impl;

import com.FreelancerUp.exception.BadRequestException;
import com.FreelancerUp.exception.ResourceNotFoundException;
import com.FreelancerUp.feature.project.repository.ProjectRepository;
import com.FreelancerUp.feature.review.dto.request.CreateReviewRequest;
import com.FreelancerUp.feature.review.dto.response.ReviewResponse;
import com.FreelancerUp.feature.review.repository.ReviewRepository;
import com.FreelancerUp.feature.review.service.ReviewService;
import com.FreelancerUp.feature.user.repository.UserRepository;
import com.FreelancerUp.model.document.Project;
import com.FreelancerUp.model.entity.Review;
import com.FreelancerUp.model.entity.User;
import com.FreelancerUp.model.enums.ProjectStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of ReviewService.
 *
 * Key Features:
 * - Create reviews for completed projects
 * - Retrieve reviews by user and project
 * - Calculate reputation scores
 * - Update review visibility
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    @Override
    @Transactional
    public ReviewResponse createReview(UUID fromUserId, CreateReviewRequest request) {
        log.info("Creating review from user {} for project {}", fromUserId, request.getProjectId());

        // Validate reviewer exists
        User fromUser = userRepository.findById(fromUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Reviewer not found"));

        // Validate recipient exists
        User toUser = userRepository.findById(request.getToUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Reviewed user not found"));

        // Validate project exists and is completed
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        if (project.getStatus() != ProjectStatus.COMPLETED) {
            throw new BadRequestException("Can only review completed projects");
        }

        // Check if review already exists
        reviewRepository.findByFromUserAndToUserAndProjectId(fromUserId, request.getToUserId(), request.getProjectId())
                .ifPresent(existing -> {
                    throw new BadRequestException("Review already exists for this project");
                });

        // Create review
        Review review = Review.builder()
                .projectId(request.getProjectId())
                .fromUser(fromUser)
                .toUser(toUser)
                .rating(request.getRating())
                .comment(request.getComment())
                .communicationRating(request.getCommunicationRating())
                .qualityRating(request.getQualityRating())
                .timelineRating(request.getTimelineRating())
                .professionalismRating(request.getProfessionalismRating())
                .responsivenessRating(request.getResponsivenessRating())
                .isVisible(true)
                .build();

        review = reviewRepository.save(review);

        log.info("Review created: {}", review.getId());
        return convertToResponse(review, project.getTitle());
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewResponse getReviewById(UUID reviewId) {
        log.info("Fetching review: {}", reviewId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        // Get project title
        String projectTitle = null;
        try {
            Project project = projectRepository.findById(review.getProjectId()).orElse(null);
            projectTitle = project != null ? project.getTitle() : null;
        } catch (Exception e) {
            log.warn("Failed to fetch project title for review: {}", reviewId);
        }

        return convertToResponse(review, projectTitle);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByProject(String projectId) {
        log.info("Fetching reviews for project: {}", projectId);

        List<Review> reviews = reviewRepository.findVisibleReviewsByProjectId(projectId);

        // Get project title once
        String projectTitle = null;
        try {
            Project project = projectRepository.findById(projectId).orElse(null);
            projectTitle = project != null ? project.getTitle() : null;
        } catch (Exception e) {
            log.warn("Failed to fetch project title: {}", projectId);
        }

        String finalProjectTitle = projectTitle;
        return reviews.stream()
                .map(review -> convertToResponse(review, finalProjectTitle))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviewsByProjectPaginated(String projectId, Pageable pageable) {
        log.info("Fetching paginated reviews for project: {}", projectId);

        Page<Review> reviewPage = reviewRepository.findByProjectIdAndIsVisibleTrue(projectId, pageable);

        // Get project title once
        String projectTitle = null;
        try {
            Project project = projectRepository.findById(projectId).orElse(null);
            projectTitle = project != null ? project.getTitle() : null;
        } catch (Exception e) {
            log.warn("Failed to fetch project title: {}", projectId);
        }

        String finalProjectTitle = projectTitle;
        return reviewPage.map(review -> convertToResponse(review, finalProjectTitle));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByUser(UUID userId) {
        log.info("Fetching reviews for user: {}", userId);

        List<Review> reviews = reviewRepository.findVisibleReviewsByToUserId(userId);

        return reviews.stream()
                .map(review -> {
                    // Get project title for each review
                    String projectTitle = null;
                    try {
                        Project project = projectRepository.findById(review.getProjectId()).orElse(null);
                        projectTitle = project != null ? project.getTitle() : null;
                    } catch (Exception e) {
                        log.warn("Failed to fetch project title for review: {}", review.getId());
                    }
                    return convertToResponse(review, projectTitle);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviewsByUserPaginated(UUID userId, Pageable pageable) {
        log.info("Fetching paginated reviews for user: {}", userId);

        Page<Review> reviewPage = reviewRepository.findByToUser_IdAndIsVisibleTrue(userId, pageable);

        return reviewPage.map(review -> {
            // Get project title for each review
            String projectTitle = null;
            try {
                Project project = projectRepository.findById(review.getProjectId()).orElse(null);
                projectTitle = project != null ? project.getTitle() : null;
            } catch (Exception e) {
                log.warn("Failed to fetch project title for review: {}", review.getId());
            }
            return convertToResponse(review, projectTitle);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsGivenByUser(UUID userId) {
        log.info("Fetching reviews given by user: {}", userId);

        List<Review> reviews = reviewRepository.findByFromUser_Id(userId);

        return reviews.stream()
                .map(review -> {
                    // Get project title for each review
                    String projectTitle = null;
                    try {
                        Project project = projectRepository.findById(review.getProjectId()).orElse(null);
                        projectTitle = project != null ? project.getTitle() : null;
                    } catch (Exception e) {
                        log.warn("Failed to fetch project title for review: {}", review.getId());
                    }
                    return convertToResponse(review, projectTitle);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ReputationData getUserReputation(UUID userId) {
        log.info("Calculating reputation for user: {}", userId);

        Double avgRating = reviewRepository.getAverageRatingForUser(userId);
        Long totalReviews = reviewRepository.getTotalReviewsForUser(userId);

        // Calculate average for each category
        List<Review> reviews = reviewRepository.findVisibleReviewsByToUserId(userId);

        if (reviews.isEmpty()) {
            return new ReputationData(0.0, 0L, null, null, null, null, null);
        }

        int commSum = 0, qualSum = 0, timeSum = 0, profSum = 0, respSum = 0;
        int commCount = 0, qualCount = 0, timeCount = 0, profCount = 0, respCount = 0;

        for (Review review : reviews) {
            if (review.getCommunicationRating() != null) {
                commSum += review.getCommunicationRating();
                commCount++;
            }
            if (review.getQualityRating() != null) {
                qualSum += review.getQualityRating();
                qualCount++;
            }
            if (review.getTimelineRating() != null) {
                timeSum += review.getTimelineRating();
                timeCount++;
            }
            if (review.getProfessionalismRating() != null) {
                profSum += review.getProfessionalismRating();
                profCount++;
            }
            if (review.getResponsivenessRating() != null) {
                respSum += review.getResponsivenessRating();
                respCount++;
            }
        }

        return new ReputationData(
                avgRating != null ? avgRating : 0.0,
                totalReviews != null ? totalReviews : 0L,
                commCount > 0 ? commSum / commCount : null,
                qualCount > 0 ? qualSum / qualCount : null,
                timeCount > 0 ? timeSum / timeCount : null,
                profCount > 0 ? profSum / profCount : null,
                respCount > 0 ? respSum / respCount : null
        );
    }

    @Override
    @Transactional
    public void updateReviewVisibility(UUID reviewId, boolean isVisible) {
        log.info("Updating visibility for review: {} to {}", reviewId, isVisible);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        review.setIsVisible(isVisible);
        reviewRepository.save(review);

        log.info("Review visibility updated: {}", reviewId);
    }

    /**
     * Convert Review entity to ReviewResponse.
     */
    private ReviewResponse convertToResponse(Review review, String projectTitle) {
        return ReviewResponse.builder()
                .id(review.getId())
                .projectId(review.getProjectId())
                .projectTitle(projectTitle)
                .fromUserId(review.getFromUser().getId())
                .fromUserName(review.getFromUser().getFullName())
                .fromUserAvatarUrl(review.getFromUser().getAvatarUrl())
                .toUserId(review.getToUser().getId())
                .toUserName(review.getToUser().getFullName())
                .rating(review.getRating())
                .comment(review.getComment())
                .communicationRating(review.getCommunicationRating())
                .qualityRating(review.getQualityRating())
                .timelineRating(review.getTimelineRating())
                .professionalismRating(review.getProfessionalismRating())
                .responsivenessRating(review.getResponsivenessRating())
                .isVisible(review.getIsVisible())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
