package com.FreelancerUp;

import com.FreelancerUp.exception.BadRequestException;
import com.FreelancerUp.exception.ResourceNotFoundException;
import com.FreelancerUp.feature.review.dto.request.CreateReviewRequest;
import com.FreelancerUp.feature.review.dto.response.ReviewResponse;
import com.FreelancerUp.feature.review.repository.ReviewRepository;
import com.FreelancerUp.feature.review.service.ReviewService;
import com.FreelancerUp.feature.review.service.impl.ReviewServiceImpl;
import com.FreelancerUp.feature.project.repository.ProjectRepository;
import com.FreelancerUp.feature.user.repository.UserRepository;
import com.FreelancerUp.model.document.Project;
import com.FreelancerUp.model.entity.Review;
import com.FreelancerUp.model.entity.User;
import com.FreelancerUp.model.enums.ProjectStatus;
import com.FreelancerUp.model.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ReviewService.
 *
 * Tests the review lifecycle:
 * 1. Create review for completed project
 * 2. Retrieve reviews by user and project
 * 3. Calculate reputation scores
 * 4. Update review visibility
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Review Service Tests")
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private UUID fromUserId;
    private UUID toUserId;
    private String projectId;
    private UUID reviewId;
    private User fromUser;
    private User toUser;
    private Project project;
    private Review review;

    @BeforeEach
    void setUp() {
        fromUserId = UUID.randomUUID();
        toUserId = UUID.randomUUID();
        projectId = "507f1f77bcf86cd799439011";
        reviewId = UUID.randomUUID();

        // Setup users
        fromUser = User.builder()
                .id(fromUserId)
                .email("client@example.com")
                .fullName("Client User")
                .role(Role.CLIENT)
                .avatarUrl("https://example.com/avatar1.jpg")
                .build();

        toUser = User.builder()
                .id(toUserId)
                .email("freelancer@example.com")
                .fullName("Freelancer User")
                .role(Role.FREELANCER)
                .avatarUrl("https://example.com/avatar2.jpg")
                .build();

        // Setup project
        project = Project.builder()
                .id(projectId)
                .title("Web Development Project")
                .description("Build a website")
                .clientId(fromUserId.toString())
                .freelancerId(toUserId.toString())
                .status(ProjectStatus.COMPLETED)
                .build();

        // Setup review
        review = Review.builder()
                .id(reviewId)
                .projectId(projectId)
                .fromUser(fromUser)
                .toUser(toUser)
                .rating(5)
                .comment("Excellent work!")
                .communicationRating(5)
                .qualityRating(5)
                .timelineRating(4)
                .professionalismRating(5)
                .responsivenessRating(5)
                .isVisible(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should create review successfully for completed project")
    void testCreateReview_Success() {
        CreateReviewRequest request = CreateReviewRequest.builder()
                .projectId(projectId)
                .toUserId(toUserId)
                .rating(5)
                .comment("Excellent work!")
                .communicationRating(5)
                .qualityRating(5)
                .timelineRating(4)
                .professionalismRating(5)
                .responsivenessRating(5)
                .build();

        when(userRepository.findById(fromUserId)).thenReturn(Optional.of(fromUser));
        when(userRepository.findById(toUserId)).thenReturn(Optional.of(toUser));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(reviewRepository.findByFromUserIdAndToUserIdAndProjectId(fromUserId, toUserId, projectId))
                .thenReturn(Optional.empty());
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        ReviewResponse response = reviewService.createReview(fromUserId, request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(reviewId);
        assertThat(response.getRating()).isEqualTo(5);
        assertThat(response.getComment()).isEqualTo("Excellent work!");
        assertThat(response.getProjectTitle()).isEqualTo("Web Development Project");

        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    @DisplayName("Should throw exception when reviewer not found")
    void testCreateReview_ReviewerNotFound() {
        CreateReviewRequest request = CreateReviewRequest.builder()
                .projectId(projectId)
                .toUserId(toUserId)
                .rating(5)
                .build();

        when(userRepository.findById(fromUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.createReview(fromUserId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Reviewer not found");

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("Should throw exception when recipient not found")
    void testCreateReview_RecipientNotFound() {
        CreateReviewRequest request = CreateReviewRequest.builder()
                .projectId(projectId)
                .toUserId(toUserId)
                .rating(5)
                .build();

        when(userRepository.findById(fromUserId)).thenReturn(Optional.of(fromUser));
        when(userRepository.findById(toUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.createReview(fromUserId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Reviewed user not found");

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("Should throw exception when project not found")
    void testCreateReview_ProjectNotFound() {
        CreateReviewRequest request = CreateReviewRequest.builder()
                .projectId(projectId)
                .toUserId(toUserId)
                .rating(5)
                .build();

        when(userRepository.findById(fromUserId)).thenReturn(Optional.of(fromUser));
        when(userRepository.findById(toUserId)).thenReturn(Optional.of(toUser));
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.createReview(fromUserId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Project not found");

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("Should throw exception when project not completed")
    void testCreateReview_ProjectNotCompleted() {
        project.setStatus(ProjectStatus.IN_PROGRESS);

        CreateReviewRequest request = CreateReviewRequest.builder()
                .projectId(projectId)
                .toUserId(toUserId)
                .rating(5)
                .build();

        when(userRepository.findById(fromUserId)).thenReturn(Optional.of(fromUser));
        when(userRepository.findById(toUserId)).thenReturn(Optional.of(toUser));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> reviewService.createReview(fromUserId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Can only review completed projects");

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("Should throw exception when review already exists")
    void testCreateReview_AlreadyExists() {
        CreateReviewRequest request = CreateReviewRequest.builder()
                .projectId(projectId)
                .toUserId(toUserId)
                .rating(5)
                .build();

        when(userRepository.findById(fromUserId)).thenReturn(Optional.of(fromUser));
        when(userRepository.findById(toUserId)).thenReturn(Optional.of(toUser));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(reviewRepository.findByFromUserIdAndToUserIdAndProjectId(fromUserId, toUserId, projectId))
                .thenReturn(Optional.of(review));

        assertThatThrownBy(() -> reviewService.createReview(fromUserId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Review already exists for this project");

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("Should get review by ID successfully")
    void testGetReviewById_Success() {
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        ReviewResponse response = reviewService.getReviewById(reviewId);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(reviewId);
        assertThat(response.getProjectTitle()).isEqualTo("Web Development Project");

        verify(reviewRepository, times(1)).findById(reviewId);
    }

    @Test
    @DisplayName("Should throw exception when review not found by ID")
    void testGetReviewById_NotFound() {
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.getReviewById(reviewId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Review not found");
    }

    @Test
    @DisplayName("Should get reviews by project successfully")
    void testGetReviewsByProject_Success() {
        List<Review> reviews = List.of(review);
        when(reviewRepository.findVisibleReviewsByProjectId(projectId)).thenReturn(reviews);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        List<ReviewResponse> responses = reviewService.getReviewsByProject(projectId);

        assertThat(responses).isNotNull();
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getId()).isEqualTo(reviewId);

        verify(reviewRepository, times(1)).findVisibleReviewsByProjectId(projectId);
    }

    @Test
    @DisplayName("Should get reviews by user successfully")
    void testGetReviewsByUser_Success() {
        List<Review> reviews = List.of(review);
        when(reviewRepository.findVisibleReviewsByToUserId(toUserId)).thenReturn(reviews);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        List<ReviewResponse> responses = reviewService.getReviewsByUser(toUserId);

        assertThat(responses).isNotNull();
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getToUserId()).isEqualTo(toUserId);

        verify(reviewRepository, times(1)).findVisibleReviewsByToUserId(toUserId);
    }

    @Test
    @DisplayName("Should get reviews given by user successfully")
    void testGetReviewsGivenByUser_Success() {
        List<Review> reviews = List.of(review);
        when(reviewRepository.findByFromUserId(fromUserId)).thenReturn(reviews);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        List<ReviewResponse> responses = reviewService.getReviewsGivenByUser(fromUserId);

        assertThat(responses).isNotNull();
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getFromUserId()).isEqualTo(fromUserId);

        verify(reviewRepository, times(1)).findByFromUserId(fromUserId);
    }

    @Test
    @DisplayName("Should calculate user reputation successfully")
    void testGetUserReputation_Success() {
        Review review2 = Review.builder()
                .id(UUID.randomUUID())
                .projectId("507f1f77bcf86cd799439012")
                .fromUser(fromUser)
                .toUser(toUser)
                .rating(4)
                .communicationRating(4)
                .qualityRating(5)
                .timelineRating(5)
                .isVisible(true)
                .build();

        List<Review> reviews = List.of(review, review2);
        when(reviewRepository.findVisibleReviewsByToUserId(toUserId)).thenReturn(reviews);
        when(reviewRepository.getAverageRatingForUser(toUserId)).thenReturn(4.5);
        when(reviewRepository.getTotalReviewsForUser(toUserId)).thenReturn(2L);

        ReviewService.ReputationData reputation = reviewService.getUserReputation(toUserId);

        assertThat(reputation).isNotNull();
        assertThat(reputation.averageRating()).isEqualTo(4.5);
        assertThat(reputation.totalReviews()).isEqualTo(2L);
        assertThat(reputation.communicationRating()).isEqualTo(4); // (5+4)/2 = 4.5 -> 4
        assertThat(reputation.qualityRating()).isEqualTo(5); // (5+5)/2 = 5
        assertThat(reputation.timelineRating()).isEqualTo(4); // (4+5)/2 = 4.5 -> 4
    }

    @Test
    @DisplayName("Should return zero reputation when no reviews")
    void testGetUserReputation_NoReviews() {
        when(reviewRepository.findVisibleReviewsByToUserId(toUserId)).thenReturn(List.of());
        when(reviewRepository.getAverageRatingForUser(toUserId)).thenReturn(null);
        when(reviewRepository.getTotalReviewsForUser(toUserId)).thenReturn(0L);

        ReviewService.ReputationData reputation = reviewService.getUserReputation(toUserId);

        assertThat(reputation).isNotNull();
        assertThat(reputation.averageRating()).isEqualTo(0.0);
        assertThat(reputation.totalReviews()).isEqualTo(0L);
        assertThat(reputation.communicationRating()).isNull();
        assertThat(reputation.qualityRating()).isNull();
    }

    @Test
    @DisplayName("Should update review visibility successfully")
    void testUpdateReviewVisibility_Success() {
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        reviewService.updateReviewVisibility(reviewId, false);

        assertThat(review.getIsVisible()).isFalse();

        verify(reviewRepository, times(1)).save(review);
    }

    @Test
    @DisplayName("Should throw exception when updating visibility for non-existent review")
    void testUpdateReviewVisibility_NotFound() {
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.updateReviewVisibility(reviewId, false))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Review not found");

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("Should get paginated reviews by project successfully")
    void testGetReviewsByProjectPaginated_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Review> reviews = List.of(review);
        Page<Review> reviewPage = new PageImpl<>(reviews, pageable, 1);

        when(reviewRepository.findByProjectIdAndIsVisibleTrue(projectId, pageable))
                .thenReturn(reviewPage);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        Page<ReviewResponse> response = reviewService.getReviewsByProjectPaginated(projectId, pageable);

        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getId()).isEqualTo(reviewId);

        verify(reviewRepository, times(1))
                .findByProjectIdAndIsVisibleTrue(projectId, pageable);
    }

    @Test
    @DisplayName("Should get paginated reviews by user successfully")
    void testGetReviewsByUserPaginated_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Review> reviews = List.of(review);
        Page<Review> reviewPage = new PageImpl<>(reviews, pageable, 1);

        when(reviewRepository.findByToUserIdAndIsVisibleTrue(toUserId, pageable))
                .thenReturn(reviewPage);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        Page<ReviewResponse> response = reviewService.getReviewsByUserPaginated(toUserId, pageable);

        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getToUserId()).isEqualTo(toUserId);

        verify(reviewRepository, times(1))
                .findByToUserIdAndIsVisibleTrue(toUserId, pageable);
    }
}
