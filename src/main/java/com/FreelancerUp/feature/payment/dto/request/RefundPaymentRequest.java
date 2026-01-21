package com.FreelancerUp.feature.payment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to refund payment from escrow back to client")
public class RefundPaymentRequest {

    @NotNull(message = "Payment ID is required")
    @Schema(description = "Payment ID (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID paymentId;

    @Schema(description = "Reason for refund", example = "Project cancelled by mutual agreement")
    private String reason;
}
