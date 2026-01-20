package com.FreelancerUp.feature.bid.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Submit bid request")
public class SubmitBidRequest {

    @NotBlank(message = "Proposal is required")
    @Size(min = 50, max = 5000, message = "Proposal must be between 50 and 5000 characters")
    private String proposal;

    @DecimalMin(value = "10.00", message = "Price must be at least $10.00")
    private BigDecimal price;

    @Min(value = 1, message = "Duration must be at least 1 day")
    @Max(value = 365, message = "Duration cannot exceed 365 days")
    private Integer estimatedDuration;
}
