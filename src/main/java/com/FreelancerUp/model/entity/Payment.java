package com.FreelancerUp.model.entity;

import com.FreelancerUp.model.enums.PaymentMethodType;
import com.FreelancerUp.model.enums.PaymentStatus;
import com.FreelancerUp.model.enums.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String projectId; // MongoDB Project ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_id", nullable = false)
    private User fromUser; // Client

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_user_id", nullable = false)
    private User toUser; // Freelancer

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private PaymentType type;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(precision = 15, scale = 2)
    private BigDecimal fee = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal netAmount;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private PaymentMethodType method;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isEscrow = true;

    @Column(name = "escrow_funded_at")
    private LocalDateTime escrowFundedAt;

    @Column(name = "escrow_released_at")
    private LocalDateTime escrowReleasedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
