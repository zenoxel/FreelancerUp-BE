package com.FreelancerUp.model.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.persistence.PrePersist;
import java.time.LocalDateTime;

@Document(collection = "messages")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    @Id
    private String id;

    private String conversationId; // UUID from PostgreSQL conversations.id
    private String projectId; // MongoDB ObjectId

    private String fromUserId; // UUID from PostgreSQL
    private String toUserId; // UUID from PostgreSQL

    private String content;

    @Builder.Default
    private Boolean isRead = false;
    private LocalDateTime readAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (readAt == null && Boolean.TRUE.equals(isRead)) {
            readAt = LocalDateTime.now();
        }
    }
}
