package com.FreelancerUp.feature.payment.dto.response;

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
@Schema(description = "Wallet information response")
public class WalletResponse {

    @Schema(description = "Wallet ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "User ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID userId;

    @Schema(description = "Available balance", example = "5000.00")
    private BigDecimal balance;

    @Schema(description = "Balance held in escrow", example = "2000.00")
    private BigDecimal escrowBalance;

    @Schema(description = "Total earned amount", example = "15000.00")
    private BigDecimal totalEarned;

    @Schema(description = "Currency code", example = "USD")
    private String currency;

    @Schema(description = "Wallet creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}
