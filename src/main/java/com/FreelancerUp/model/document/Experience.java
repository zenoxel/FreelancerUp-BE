package com.FreelancerUp.model.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "experiences")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Experience {
    @Id
    private String id;

    private String freelancerId; // UUID from PostgreSQL users.id

    private String title;
    private String company;
    private String location;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @Builder.Default
    private Boolean isCurrentJob = false;

    private String description;
    private List<String> skills;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
