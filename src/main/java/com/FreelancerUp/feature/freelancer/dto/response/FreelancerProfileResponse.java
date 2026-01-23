package com.FreelancerUp.feature.freelancer.dto.response;

import com.FreelancerUp.model.enums.Availability;
import com.FreelancerUp.model.enums.ProficiencyLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Freelancer profile response")
public class FreelancerProfileResponse {

    private UUID id;
    private String email;
    private String fullName;
    private String avatarUrl;

    // Freelancer-specific fields
    private String bio;
    private BigDecimal hourlyRate;
    private Availability availability;

    // Statistics
    private BigDecimal totalEarned;
    private Integer completedProjects;
    private Double successRate;

    private List<FreelancerSkillDTO> skills;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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
