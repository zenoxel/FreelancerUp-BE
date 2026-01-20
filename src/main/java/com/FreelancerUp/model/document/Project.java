package com.FreelancerUp.model.document;

import com.FreelancerUp.model.enums.ProjectStatus;
import com.FreelancerUp.model.enums.ProjectType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "projects")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Project {
    @Id
    private String id;

    private String clientId; // UUID from PostgreSQL users.id
    private String freelancerId; // UUID from PostgreSQL users.id (nullable)

    private String title;
    private String description;
    private String requirements;

    private List<String> skills;

    private ProjectBudget budget;

    private Integer duration; // in days

    @Enumerated(EnumType.STRING)
    private ProjectStatus status = ProjectStatus.OPEN;

    @Enumerated(EnumType.STRING)
    private ProjectType type;

    private LocalDateTime deadline;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    private String contractId; // UUID from PostgreSQL contracts.id

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Embedded class for budget
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProjectBudget {
        private BigDecimal minAmount;
        private BigDecimal maxAmount;

        @Builder.Default
        private String currency = "USD";

        @Builder.Default
        private Boolean isNegotiable = false;
    }
}
