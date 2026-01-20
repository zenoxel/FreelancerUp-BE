package com.FreelancerUp.feature.client.dto.response;

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
@Schema(description = "Client statistics response")
public class ClientStatsResponse {

    private UUID clientId;
    private String companyName;

    // Project statistics
    private Integer totalProjects;
    private Integer activeProjects;
    private Integer completedProjects;

    // Financial statistics
    private BigDecimal totalSpent;
    private BigDecimal pendingEscrow;

    // Rating statistics
    private Double averageRating;
    private Integer totalReviews;
}
