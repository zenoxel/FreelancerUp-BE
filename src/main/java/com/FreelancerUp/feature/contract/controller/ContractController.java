package com.FreelancerUp.feature.contract.controller;

import com.FreelancerUp.feature.common.dto.ApiResponse;
import com.FreelancerUp.feature.contract.dto.response.ContractResponse;
import com.FreelancerUp.feature.contract.service.ContractService;
import com.FreelancerUp.model.enums.ContractStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for Contract operations.
 *
 * Endpoints:
 * - GET /api/v1/contracts/{id} - Get contract by ID
 * - GET /api/v1/contracts/project/{projectId} - Get contract by project
 * - GET /api/v1/contracts/my - Get current user's contracts
 * - PATCH /api/v1/contracts/{id}/status - Update contract status
 * - PATCH /api/v1/contracts/{id}/complete - Complete contract
 */
@RestController
@RequestMapping("/api/v1/contracts")
@RequiredArgsConstructor
@Tag(name = "Contract", description = "Contract management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ContractController {

    private final ContractService contractService;

    @GetMapping("/{contractId}")
    @Operation(summary = "Get contract by ID", description = "Returns contract information by ID")
    public ResponseEntity<ApiResponse<ContractResponse>> getContractById(
            @Parameter(description = "Contract ID") @PathVariable UUID contractId) {

        ContractResponse contract = contractService.getContractById(contractId);
        return ResponseEntity.ok(ApiResponse.success(contract));
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "Get contract by project", description = "Returns the contract for a specific project")
    public ResponseEntity<ApiResponse<ContractResponse>> getContractByProjectId(
            @Parameter(description = "Project ID (MongoDB)") @PathVariable String projectId) {

        ContractResponse contract = contractService.getContractByProjectId(projectId);
        return ResponseEntity.ok(ApiResponse.success(contract));
    }

    @GetMapping("/my")
    @Operation(summary = "Get current user's contracts", description = "Returns all contracts for the authenticated user (as client or freelancer)")
    public ResponseEntity<List<ContractResponse>> getMyContracts(
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = UUID.fromString(userDetails.getUsername());
        List<ContractResponse> contracts = contractService.getContractsByUser(userId);
        return ResponseEntity.ok(contracts);
    }

    @GetMapping("/my/client")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Get current user's client contracts", description = "Returns all contracts where the authenticated user is the client")
    public ResponseEntity<List<ContractResponse>> getMyClientContracts(
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = UUID.fromString(userDetails.getUsername());
        List<ContractResponse> contracts = contractService.getContractsByClient(userId);
        return ResponseEntity.ok(contracts);
    }

    @GetMapping("/my/freelancer")
    @PreAuthorize("hasRole('FREELANCER')")
    @Operation(summary = "Get current user's freelancer contracts", description = "Returns all contracts where the authenticated user is the freelancer")
    public ResponseEntity<List<ContractResponse>> getMyFreelancerContracts(
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = UUID.fromString(userDetails.getUsername());
        List<ContractResponse> contracts = contractService.getContractsByFreelancer(userId);
        return ResponseEntity.ok(contracts);
    }

    @PatchMapping("/{contractId}/status")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @Operation(summary = "Update contract status", description = "Updates the status of a contract (CLIENT can only complete their own contracts)")
    public ResponseEntity<ApiResponse<ContractResponse>> updateContractStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Contract ID") @PathVariable UUID contractId,
            @Parameter(description = "New contract status") @RequestParam ContractStatus status) {

        // For CLIENT role, verify ownership before allowing status change
        if (userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENT"))) {

            ContractResponse existing = contractService.getContractById(contractId);
            UUID userId = UUID.fromString(userDetails.getUsername());

            if (!existing.getClientId().equals(userId)) {
                return ResponseEntity.status(403)
                        .build();
            }
        }

        ContractResponse contract = contractService.updateContractStatus(contractId, status);
        return ResponseEntity.ok(ApiResponse.success("Contract status updated successfully", contract));
    }

    @PatchMapping("/{contractId}/complete")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Complete contract", description = "Marks a contract as completed (only accessible by the client)")
    public ResponseEntity<ApiResponse<ContractResponse>> completeContract(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Contract ID") @PathVariable UUID contractId) {

        // Verify ownership
        ContractResponse existing = contractService.getContractById(contractId);
        UUID userId = UUID.fromString(userDetails.getUsername());

        if (!existing.getClientId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }

        ContractResponse contract = contractService.completeContract(contractId);
        return ResponseEntity.ok(ApiResponse.success("Contract completed successfully", contract));
    }
}
