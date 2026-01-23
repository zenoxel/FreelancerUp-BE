package com.FreelancerUp.feature.freelancer.dto.request;

import com.FreelancerUp.model.enums.Availability;
import com.FreelancerUp.model.enums.ProficiencyLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@Schema(description = "Register freelancer request")
public class RegisterFreelancerRequest {

    @Size(max = 2000)
    private String bio;

    @NotNull(message = "Hourly rate is required")
    @DecimalMin(value = "1.0", message = "Hourly rate must be at least 1")
    private BigDecimal hourlyRate;

    private Availability availability;

    @NotEmpty(message = "At least one skill is required")
    @Valid
    private List<FreelancerSkillDTO> skills;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Freelancer skill")
    public static class FreelancerSkillDTO {
        private String skillId;
        private String name;

        @NotNull
        private ProficiencyLevel proficiencyLevel;

        private Integer yearsOfExperience;
    }
}
