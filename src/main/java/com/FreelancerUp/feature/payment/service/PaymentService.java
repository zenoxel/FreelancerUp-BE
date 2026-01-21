package com.FreelancerUp.feature.payment.service;

import com.FreelancerUp.feature.payment.dto.request.FundEscrowRequest;
import com.FreelancerUp.feature.payment.dto.request.RefundPaymentRequest;
import com.FreelancerUp.feature.payment.dto.request.ReleasePaymentRequest;
import com.FreelancerUp.feature.payment.dto.response.PaymentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for Payment operations.
 * Handles escrow payments, funding, releasing, and refunds.
 *
 * CRITICAL: All payment operations must be @Transactional for data integrity.
 */
public interface PaymentService {

    /**
     * Fund escrow for a project.
     * Flow: Client Wallet -> Escrow Hold -> ESCROW_HOLD transaction
     *
     * @param clientId Client User ID
     * @param request Fund escrow request
     * @return PaymentResponse
     */
    PaymentResponse fundEscrow(UUID clientId, FundEscrowRequest request);

    /**
     * Release payment from escrow to freelancer.
     * Flow: Escrow -> Freelancer Wallet -> ESCROW_RELEASE transaction
     *
     * @param clientId Client User ID (for authorization)
     * @param request Release payment request
     * @return PaymentResponse
     */
    PaymentResponse releasePayment(UUID clientId, ReleasePaymentRequest request);

    /**
     * Refund payment from escrow back to client.
     * Flow: Escrow -> Client Wallet -> REFUND status
     *
     * @param paymentId Payment ID
     * @param request Refund payment request
     * @return PaymentResponse
     */
    PaymentResponse refundPayment(UUID paymentId, RefundPaymentRequest request);

    /**
     * Get payment by ID.
     *
     * @param paymentId Payment ID
     * @return PaymentResponse
     */
    PaymentResponse getPaymentById(UUID paymentId);

    /**
     * Get payments by project ID.
     *
     * @param projectId Project ID (MongoDB)
     * @return List of PaymentResponse
     */
    List<PaymentResponse> getPaymentsByProjectId(String projectId);

    /**
     * Get payments for user (as client or freelancer).
     *
     * @param userId User ID
     * @param pageable Pageable
     * @return Page of PaymentResponse
     */
    Page<PaymentResponse> getUserPayments(UUID userId, Pageable pageable);

    /**
     * Get payments by status.
     *
     * @param status Payment status
     * @return List of PaymentResponse
     */
    List<PaymentResponse> getPaymentsByStatus(com.FreelancerUp.model.enums.PaymentStatus status);
}
