package com.FreelancerUp.feature.project.dto.request;

import com.FreelancerUp.feature.common.dto.ProjectBudgetDTO;
import com.FreelancerUp.model.enums.ProjectType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
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
@Schema(description = "Create project request")
public class CreateProjectRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 10, max = 200)
    private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 50, max = 5000)
    private String description;

    @Size(max = 2000)
    private String requirements;

    @NotEmpty(message = "At least one skill is required")
    private List<String> skills;

    @Valid
    @NotNull(message = "Budget information is required")
    private ProjectBudgetDTO budget;

    @Min(value = 1, message = "Duration must be at least 1 day")
    @Max(value = 365, message = "Duration cannot exceed 365 days")
    private Integer duration;

    @NotNull(message = "Project type is required")
    private ProjectType type;

    @Future(message = "Deadline must be in the future")
    private LocalDateTime deadline;
}
