package com.FreelancerUp.feature.project.dto.request;

import com.FreelancerUp.feature.common.dto.ProjectBudgetDTO;
import com.FreelancerUp.model.enums.ProjectType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
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
@Schema(description = "Update project request")
public class UpdateProjectRequest {

    @Size(min = 10, max = 200)
    private String title;

    @Size(min = 50, max = 5000)
    private String description;

    @Size(max = 2000)
    private String requirements;

    private List<String> skills;

    @Valid
    private ProjectBudgetDTO budget;

    @Min(value = 1, message = "Duration must be at least 1 day")
    @Max(value = 365, message = "Duration cannot exceed 365 days")
    private Integer duration;

    private ProjectType type;

    @Future(message = "Deadline must be in the future")
    private LocalDateTime deadline;
}
