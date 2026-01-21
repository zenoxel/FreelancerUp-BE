package com.FreelancerUp.feature.payment.dto.request;

import com.FreelancerUp.model.enums.PaymentMethodType;
import com.FreelancerUp.model.enums.PaymentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to fund escrow for a project")
public class FundEscrowRequest {

    @NotNull(message = "Project ID is required")
    @Schema(description = "MongoDB Project ID", example = "507f1f77bcf86cd799439011")
    private String projectId;

    @NotNull(message = "Freelancer ID is required")
    @Schema(description = "Freelancer User ID (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID freelancerId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.00", message = "Amount must be at least 1.00")
    @Schema(description = "Payment amount", example = "1000.00")
    private BigDecimal amount;

    @NotNull(message = "Payment type is required")
    @Schema(description = "Payment type", example = "MILESTONE")
    private PaymentType type;

    @NotNull(message = "Payment method is required")
    @Schema(description = "Payment method", example = "WALLET_BALANCE")
    private PaymentMethodType method;

    @Schema(description = "Optional description for the payment", example = "Initial payment for web development project")
    private String description;
}
