package com.FreelancerUp.feature.project.dto.response;

import com.FreelancerUp.feature.common.dto.ProjectBudgetDTO;
import com.FreelancerUp.model.enums.ProjectStatus;
import com.FreelancerUp.model.enums.ProjectType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Project detail response")
public class ProjectDetailResponse {

    private String id;
    private String clientId;

    // Client information
    private UUID clientIdUUID;
    private String clientEmail;
    private String clientFullName;
    private String clientAvatarUrl;
    private String clientCompanyName;

    // Freelancer information (if assigned)
    private UUID freelancerIdUUID;
    private String freelancerEmail;
    private String freelancerFullName;
    private String freelancerAvatarUrl;

    private String title;
    private String description;
    private String requirements;

    private List<String> skills;
    private ProjectBudgetDTO budget;

    private Integer duration;
    private ProjectStatus status;
    private ProjectType type;

    private LocalDateTime deadline;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    private String contractId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Statistics
    private Integer totalBids;
    private Integer averageBidAmount;
}
