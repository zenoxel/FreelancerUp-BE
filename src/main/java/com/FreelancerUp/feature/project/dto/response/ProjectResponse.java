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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Project response")
public class ProjectResponse {

    private String id;
    private String clientId;

    // Client information (simplified)
    private String clientName;
    private String clientAvatarUrl;

    private String title;
    private String description;

    private List<String> skills;
    private ProjectBudgetDTO budget;

    private Integer duration;
    private ProjectStatus status;
    private ProjectType type;

    private LocalDateTime deadline;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
