package com.FreelancerUp.feature.contract.dto.response;

import com.FreelancerUp.model.enums.ContractStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for Contract information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Contract information response")
public class ContractResponse {

    @Schema(description = "Contract ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Project ID (MongoDB)", example = "507f1f77bcf86cd799439011")
    private String projectId;

    @Schema(description = "Client User ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID clientId;

    @Schema(description = "Client full name", example = "John Doe")
    private String clientName;

    @Schema(description = "Client email", example = "john@example.com")
    private String clientEmail;

    @Schema(description = "Freelancer User ID", example = "650e8400-e29b-41d4-a716-446655440001")
    private UUID freelancerId;

    @Schema(description = "Freelancer full name", example = "Jane Smith")
    private String freelancerName;

    @Schema(description = "Freelancer email", example = "jane@example.com")
    private String freelancerEmail;

    @Schema(description = "Contract status", example = "ACTIVE")
    private ContractStatus status;

    @Schema(description = "Contract creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}
