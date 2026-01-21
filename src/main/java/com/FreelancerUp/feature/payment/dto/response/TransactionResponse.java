package com.FreelancerUp.feature.payment.dto.response;

import com.FreelancerUp.model.enums.TransactionStatus;
import com.FreelancerUp.model.enums.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Transaction information response")
public class TransactionResponse {

    @Schema(description = "Transaction ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Wallet ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID walletId;

    @Schema(description = "User ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID userId;

    @Schema(description = "Transaction type", example = "CREDIT")
    private TransactionType type;

    @Schema(description = "Transaction status", example = "COMPLETED")
    private TransactionStatus status;

    @Schema(description = "Transaction amount", example = "1000.00")
    private BigDecimal amount;

    @Schema(description = "Transaction description", example = "Payment for project web development")
    private String description;

    @Schema(description = "Reference ID (project/payment ID)", example = "507f1f77bcf86cd799439011")
    private String referenceId;

    @Schema(description = "Balance before transaction", example = "5000.00")
    private BigDecimal balanceBefore;

    @Schema(description = "Balance after transaction", example = "6000.00")
    private BigDecimal balanceAfter;

    @Schema(description = "Transaction creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Transaction completion timestamp")
    private LocalDateTime completedAt;
}
