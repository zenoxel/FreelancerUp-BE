package com.FreelancerUp.feature.payment.controller;

import com.FreelancerUp.feature.common.dto.ApiResponse;
import com.FreelancerUp.feature.payment.dto.request.FundEscrowRequest;
import com.FreelancerUp.feature.payment.dto.request.RefundPaymentRequest;
import com.FreelancerUp.feature.payment.dto.request.ReleasePaymentRequest;
import com.FreelancerUp.feature.payment.dto.response.PaymentResponse;
import com.FreelancerUp.feature.payment.service.PaymentService;
import com.FreelancerUp.model.enums.PaymentStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for Payment and Escrow operations.
 *
 * CRITICAL: All payment operations are protected with @Transactional
 * and proper authorization checks.
 *
 * Endpoints:
 * - POST /api/v1/payments/escrow/fund - Fund escrow (CLIENT only)
 * - POST /api/v1/payments/escrow/release - Release payment (CLIENT only)
 * - POST /api/v1/payments/escrow/refund - Refund payment (SYSTEM/ADMIN)
 * - GET /api/v1/payments/{id} - Get payment by ID
 * - GET /api/v1/payments/project/{projectId} - Get payments by project
 * - GET /api/v1/payments/my - Get current user's payments
 */
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payment", description = "Payment and escrow management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/escrow/fund")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Fund escrow for a project", description = "Client funds escrow for a project with an accepted bid")
    public ResponseEntity<ApiResponse<PaymentResponse>> fundEscrow(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody FundEscrowRequest request) {

        UUID clientId = UUID.fromString(userDetails.getUsername());
        PaymentResponse payment = paymentService.fundEscrow(clientId, request);
        return ResponseEntity.ok(ApiResponse.success("Escrow funded successfully", payment));
    }

    @PostMapping("/escrow/release")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Release payment from escrow", description = "Client releases payment from escrow to freelancer")
    public ResponseEntity<ApiResponse<PaymentResponse>> releasePayment(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ReleasePaymentRequest request) {

        UUID clientId = UUID.fromString(userDetails.getUsername());
        PaymentResponse payment = paymentService.releasePayment(clientId, request);
        return ResponseEntity.ok(ApiResponse.success("Payment released successfully", payment));
    }

    @PostMapping("/escrow/refund")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Refund payment from escrow", description = "Admin refunds payment back to client (for disputes/cancellations)")
    public ResponseEntity<ApiResponse<PaymentResponse>> refundPayment(
            @Valid @RequestBody RefundPaymentRequest request) {

        PaymentResponse payment = paymentService.refundPayment(request.getPaymentId(), request);
        return ResponseEntity.ok(ApiResponse.success("Payment refunded successfully", payment));
    }

    @GetMapping("/{paymentId}")
    @Operation(summary = "Get payment by ID", description = "Returns payment information by ID")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(
            @Parameter(description = "Payment ID") @PathVariable UUID paymentId) {

        PaymentResponse payment = paymentService.getPaymentById(paymentId);
        return ResponseEntity.ok(ApiResponse.success(payment));
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "Get payments by project", description = "Returns all payments for a specific project")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentsByProject(
            @Parameter(description = "Project ID (MongoDB)") @PathVariable String projectId) {

        List<PaymentResponse> payments = paymentService.getPaymentsByProjectId(projectId);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    @GetMapping("/my")
    @Operation(summary = "Get current user's payments", description = "Returns all payments for the authenticated user (as client or freelancer)")
    public ResponseEntity<Page<PaymentResponse>> getMyPayments(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "DESC") String sortDirection) {

        UUID userId = UUID.fromString(userDetails.getUsername());
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), "createdAt");
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PaymentResponse> payments = paymentService.getUserPayments(userId, pageable);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get payments by status", description = "Returns all payments with a specific status")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentsByStatus(
            @Parameter(description = "Payment status") @PathVariable PaymentStatus status) {

        List<PaymentResponse> payments = paymentService.getPaymentsByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }
}
