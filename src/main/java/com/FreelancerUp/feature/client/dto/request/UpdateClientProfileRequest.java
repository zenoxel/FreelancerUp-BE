package com.FreelancerUp.feature.client.dto.request;

import com.FreelancerUp.feature.common.dto.PaymentMethodDTO;
import com.FreelancerUp.model.enums.CompanySize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Update client profile request")
public class UpdateClientProfileRequest {

    @Size(min = 2, max = 255)
    private String companyName;

    @Size(max = 100)
    private String industry;

    private CompanySize companySize;

    @Valid
    private List<PaymentMethodDTO> paymentMethods;
}
