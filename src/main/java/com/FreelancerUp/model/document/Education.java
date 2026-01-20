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

@Document(collection = "education")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Education {
    @Id
    private String id;

    private String freelancerId; // UUID from PostgreSQL users.id

    private String school;
    private String degree;
    private String fieldOfStudy;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private String description;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
