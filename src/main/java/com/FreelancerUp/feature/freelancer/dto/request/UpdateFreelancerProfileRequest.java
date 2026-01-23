package com.FreelancerUp.feature.freelancer.dto.request;

import com.FreelancerUp.model.enums.Availability;
import com.FreelancerUp.model.enums.ProficiencyLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
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
@Schema(description = "Update freelancer profile request")
public class UpdateFreelancerProfileRequest {

    private String bio;

    @DecimalMin(value = "1.0", message = "Hourly rate must be at least 1")
    private BigDecimal hourlyRate;

    private Availability availability;

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

        private ProficiencyLevel proficiencyLevel;

        private Integer yearsOfExperience;
    }
}
