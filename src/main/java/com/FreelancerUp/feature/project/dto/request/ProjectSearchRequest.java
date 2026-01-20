package com.FreelancerUp.feature.project.dto.request;

import com.FreelancerUp.model.enums.ProjectStatus;
import com.FreelancerUp.model.enums.ProjectType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Project search request")
public class ProjectSearchRequest {

    private String keyword;

    private List<String> skills;

    private BigDecimal minBudget;
    private BigDecimal maxBudget;

    private ProjectType type;

    private List<ProjectStatus> statuses;

    @Builder.Default
    private String sortBy = "createdAt"; // createdAt, deadline, budget

    @Builder.Default
    private String sortDirection = "DESC"; // ASC, DESC

    @Builder.Default
    private Integer page = 0;

    @Builder.Default
    private Integer size = 20;
}
