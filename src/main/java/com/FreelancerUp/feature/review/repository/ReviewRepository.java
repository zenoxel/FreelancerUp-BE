package com.FreelancerUp.feature.review.repository;

import com.FreelancerUp.model.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    Optional<Review> findByFromUserIdAndToUserIdAndProjectId(UUID fromUserId, UUID toUserId, String projectId);

    List<Review> findByProjectId(String projectId);

    List<Review> findByFromUserId(UUID fromUserId);

    List<Review> findByToUserId(UUID toUserId);

    @Query("SELECT r FROM Review r WHERE r.toUserId = :userId AND r.isVisible = true ORDER BY r.createdAt DESC")
    List<Review> findVisibleReviewsByToUserId(@Param("userId") UUID userId);

    @Query("SELECT r FROM Review r WHERE r.projectId = :projectId AND r.isVisible = true ORDER BY r.createdAt DESC")
    List<Review> findVisibleReviewsByProjectId(@Param("projectId") String projectId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.toUser.id = :userId AND r.isVisible = true")
    Double getAverageRatingForUser(@Param("userId") UUID userId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.toUser.id = :userId AND r.isVisible = true")
    Long getTotalReviewsForUser(@Param("userId") UUID userId);

    Page<Review> findByToUserIdAndIsVisibleTrue(UUID toUserId, Pageable pageable);

    Page<Review> findByProjectIdAndIsVisibleTrue(String projectId, Pageable pageable);
}
