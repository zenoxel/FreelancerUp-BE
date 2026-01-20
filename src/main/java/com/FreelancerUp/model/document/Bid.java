package com.FreelancerUp.model.document;

import com.FreelancerUp.model.enums.BidStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "bids")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Bid {
    @Id
    private String id;

    private String projectId; // MongoDB ObjectId
    private String freelancerId; // UUID from PostgreSQL users.id

    private String proposal;
    private BigDecimal price;
    private Integer estimatedDuration; // in days

    @Enumerated(EnumType.STRING)
    private BidStatus status = BidStatus.PENDING;

    private LocalDateTime submittedAt;
    private LocalDateTime respondedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
