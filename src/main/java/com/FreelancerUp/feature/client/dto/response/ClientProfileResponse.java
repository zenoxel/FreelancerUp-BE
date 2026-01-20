package com.FreelancerUp.feature.client.dto.response;

import com.FreelancerUp.feature.common.dto.PaymentMethodDTO;
import com.FreelancerUp.model.enums.CompanySize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Client profile response")
public class ClientProfileResponse {

    private UUID id;
    private String email;
    private String fullName;
    private String avatarUrl;

    // Client-specific fields
    private String companyName;
    private String industry;
    private CompanySize companySize;
    private List<PaymentMethodDTO> paymentMethods;

    // Statistics
    private BigDecimal totalSpent;
    private Integer postedProjects;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
