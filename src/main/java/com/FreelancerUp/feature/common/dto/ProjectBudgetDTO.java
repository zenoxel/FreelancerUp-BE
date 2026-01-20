package com.FreelancerUp.feature.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Project budget DTO")
public class ProjectBudgetDTO {

    @NotNull(message = "Minimum amount is required")
    @DecimalMin(value = "0.01", message = "Minimum amount must be greater than 0")
    private BigDecimal minAmount;

    @NotNull(message = "Maximum amount is required")
    @DecimalMin(value = "0.01", message = "Maximum amount must be greater than 0")
    private BigDecimal maxAmount;

    @Builder.Default
    private String currency = "USD";

    @Builder.Default
    private Boolean isNegotiable = false;
}
