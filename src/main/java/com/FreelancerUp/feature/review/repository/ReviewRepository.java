package com.FreelancerUp.feature.review.repository;

import com.FreelancerUp.model.entity.Review;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    List<Review> findByProjectId(String projectId);

    List<Review> findByFromUserId(UUID fromUserId);

    List<Review> findByToUserId(UUID toUserId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.toUser.id = :userId")
    Double getAverageRatingForUser(@Param("userId") UUID userId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.toUser.id = :userId")
    Long getTotalReviewsForUser(@Param("userId") UUID userId);

    List<Review> findByToUserIdAndIsVisibleTrue(UUID toUserId, Pageable pageable);
}
