package com.FreelancerUp.feature.payment.dto.response;

import com.FreelancerUp.model.enums.PaymentMethodType;
import com.FreelancerUp.model.enums.PaymentStatus;
import com.FreelancerUp.model.enums.PaymentType;
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
@Schema(description = "Payment information response")
public class PaymentResponse {

    @Schema(description = "Payment ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Project ID (MongoDB)", example = "507f1f77bcf86cd799439011")
    private String projectId;

    @Schema(description = "Client User ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID fromUserId;

    @Schema(description = "Freelancer User ID", example = "650e8400-e29b-41d4-a716-446655440001")
    private UUID toUserId;

    @Schema(description = "Client full name", example = "John Doe")
    private String fromUserName;

    @Schema(description = "Freelancer full name", example = "Jane Smith")
    private String toUserName;

    @Schema(description = "Payment type", example = "MILESTONE")
    private PaymentType type;

    @Schema(description = "Payment status", example = "ESCROW_HOLD")
    private PaymentStatus status;

    @Schema(description = "Payment amount", example = "1000.00")
    private BigDecimal amount;

    @Schema(description = "Platform fee", example = "50.00")
    private BigDecimal fee;

    @Schema(description = "Net amount (amount - fee)", example = "950.00")
    private BigDecimal netAmount;

    @Schema(description = "Payment method", example = "WALLET_BALANCE")
    private PaymentMethodType method;

    @Schema(description = "Is escrow payment", example = "true")
    private Boolean isEscrow;

    @Schema(description = "Escrow funded timestamp")
    private LocalDateTime escrowFundedAt;

    @Schema(description = "Escrow released timestamp")
    private LocalDateTime escrowReleasedAt;

    @Schema(description = "Payment creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Payment completion timestamp")
    private LocalDateTime completedAt;
}
