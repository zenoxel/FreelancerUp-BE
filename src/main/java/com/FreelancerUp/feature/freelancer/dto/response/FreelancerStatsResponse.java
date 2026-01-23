package com.FreelancerUp.feature.freelancer.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Freelancer statistics response")
public class FreelancerStatsResponse {

    private UUID freelancerId;
    private String fullName;

    private Integer totalProjects;
    private Integer activeProjects;
    private Integer completedProjects;

    private BigDecimal totalEarned;
    private BigDecimal availableBalance;
    private BigDecimal escrowBalance;

    private Double averageRating;
    private Integer totalReviews;
    private Double successRate;
}
