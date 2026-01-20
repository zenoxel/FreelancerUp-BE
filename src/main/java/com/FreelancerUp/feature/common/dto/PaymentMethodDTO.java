package com.FreelancerUp.feature.common.dto;

import com.FreelancerUp.model.enums.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payment method DTO")
public class PaymentMethodDTO {

    @NotNull(message = "Payment type is required")
    private PaymentMethod type;

    private String provider;

    private String last4Digits;

    private String accountEmail;

    @Builder.Default
    private Boolean isDefault = false;
}
