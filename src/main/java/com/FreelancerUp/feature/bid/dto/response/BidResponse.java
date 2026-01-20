package com.FreelancerUp.feature.bid.dto.response;

import com.FreelancerUp.model.enums.BidStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Bid response")
public class BidResponse {

    private String id;
    private String projectId;

    // Freelancer information
    private UUID freelancerId;
    private String freelancerEmail;
    private String freelancerFullName;
    private String freelancerAvatarUrl;

    private String proposal;
    private BigDecimal price;
    private Integer estimatedDuration;

    private BidStatus status;

    private LocalDateTime submittedAt;
    private LocalDateTime respondedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
