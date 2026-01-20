package com.FreelancerUp.model.document;

import com.FreelancerUp.model.enums.Availability;
import com.FreelancerUp.model.enums.ProficiencyLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "freelancers")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Freelancer {
    @Id
    private String id;

    private String userId; // UUID from PostgreSQL users.id

    private String bio;
    private BigDecimal hourlyRate;

    @Builder.Default
    private Availability availability = Availability.AVAILABLE;

    @Builder.Default
    private BigDecimal totalEarned = BigDecimal.ZERO;

    @Builder.Default
    private Integer completedProjects = 0;

    @Builder.Default
    private Double successRate = 0.0;

    @Builder.Default
    private List<FreelancerSkill> skills = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Embedded class for skills
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FreelancerSkill {
        private String skillId;
        private String name;
        private ProficiencyLevel proficiencyLevel;
        private Integer yearsOfExperience;
    }
}
