package com.FreelancerUp.feature.payment.service.impl;

import com.FreelancerUp.exception.BadRequestException;
import com.FreelancerUp.exception.ResourceNotFoundException;
import com.FreelancerUp.feature.bid.repository.BidRepository;
import com.FreelancerUp.feature.payment.dto.request.FundEscrowRequest;
import com.FreelancerUp.feature.payment.dto.request.RefundPaymentRequest;
import com.FreelancerUp.feature.payment.dto.request.ReleasePaymentRequest;
import com.FreelancerUp.feature.payment.dto.response.PaymentResponse;
import com.FreelancerUp.feature.payment.repository.PaymentRepository;
import com.FreelancerUp.feature.payment.repository.WalletRepository;
import com.FreelancerUp.feature.payment.service.PaymentService;
import com.FreelancerUp.feature.payment.service.WalletService;
import com.FreelancerUp.feature.project.repository.ProjectRepository;
import com.FreelancerUp.feature.user.repository.UserRepository;
import com.FreelancerUp.model.document.Bid;
import com.FreelancerUp.model.document.Project;
import com.FreelancerUp.model.entity.Payment;
import com.FreelancerUp.model.entity.Wallet;
import com.FreelancerUp.model.enums.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of PaymentService.
 *
 * CRITICAL: All payment operations MUST be @Transactional for ACID compliance.
 * - Atomic wallet updates
 * - Double-entry accounting (credit = debit)
 * - Escrow state machine validation
 *
 * Escrow Flow:
 * 1. Fund Escrow: Client Wallet -> Escrow -> ESCROW_HOLD status
 * 2. Release Payment: Escrow -> Freelancer Wallet -> ESCROW_RELEASE status
 * 3. Refund: Escrow -> Client Wallet -> REFUNDED status
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final WalletRepository walletRepository;
    private final WalletService walletService;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final BidRepository bidRepository;

    @Value("${app.payment.platform-fee-percentage:5.0}")
    private BigDecimal platformFeePercentage;

    @Override
    @Transactional
    public PaymentResponse fundEscrow(UUID clientId, FundEscrowRequest request) {
        log.info("Funding escrow for project: {} by client: {}", request.getProjectId(), clientId);

        // Validate client exists
        if (!userRepository.existsById(clientId)) {
            throw new ResourceNotFoundException("Client not found");
        }

        // Validate project exists
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        // Verify project ownership
        if (!project.getClientId().equals(clientId.toString())) {
            throw new BadRequestException("You can only fund escrow for your own projects");
        }

        // Validate freelancer exists
        if (!userRepository.existsById(request.getFreelancerId())) {
            throw new ResourceNotFoundException("Freelancer not found");
        }

        // Check if there's an accepted bid for this project and freelancer
        Bid acceptedBid = bidRepository.findByProjectIdAndStatus(
                request.getProjectId(),
                com.FreelancerUp.model.enums.BidStatus.ACCEPTED
        ).orElse(null);

        if (acceptedBid == null || !acceptedBid.getFreelancerId().equals(request.getFreelancerId().toString())) {
            throw new BadRequestException("No accepted bid found for this freelancer on this project");
        }

        // Check if payment already exists for this project
        List<Payment> existingPayments = paymentRepository.findByProjectId(request.getProjectId());
        for (Payment existing : existingPayments) {
            if (existing.getStatus() == PaymentStatus.ESCROW_HOLD ||
                existing.getStatus() == PaymentStatus.RELEASED ||
                existing.getStatus() == PaymentStatus.COMPLETED) {
                throw new BadRequestException("Payment already exists for this project");
            }
        }

        // Get client wallet
        Wallet clientWallet = walletRepository.findByUserId(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client wallet not found"));

        // Calculate fee and net amount
        BigDecimal fee = request.getAmount().multiply(platformFeePercentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal netAmount = request.getAmount().subtract(fee);

        // Fund escrow - DEBIT from client wallet, HOLD in escrow
        String description = String.format("Escrow payment for project: %s", project.getTitle());
        walletService.holdEscrow(clientWallet.getId(), request.getAmount(), description, request.getProjectId());

        // Create payment record
        Payment payment = Payment.builder()
                .projectId(request.getProjectId())
                .fromUser(userRepository.findById(clientId).orElse(null))
                .toUser(userRepository.findById(request.getFreelancerId()).orElse(null))
                .type(request.getType())
                .status(PaymentStatus.ESCROW_HOLD)
                .amount(request.getAmount())
                .fee(fee)
                .netAmount(netAmount)
                .method(request.getMethod())
                .isEscrow(true)
                .escrowFundedAt(LocalDateTime.now())
                .build();

        payment = paymentRepository.save(payment);

        log.info("Escrow funded successfully. Payment ID: {}, Amount: {}", payment.getId(), request.getAmount());
        return convertToResponse(payment);
    }

    @Override
    @Transactional
    public PaymentResponse releasePayment(UUID clientId, ReleasePaymentRequest request) {
        log.info("Releasing payment: {} by client: {}", request.getPaymentId(), clientId);

        // Get payment
        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        // Verify ownership
        if (!payment.getFromUser().getId().equals(clientId)) {
            throw new BadRequestException("You can only release your own payments");
        }

        // Validate payment status
        if (payment.getStatus() != PaymentStatus.ESCROW_HOLD) {
            throw new BadRequestException(
                    String.format("Cannot release payment with status: %s", payment.getStatus())
            );
        }

        // Get client wallet (where escrow is held)
        Wallet clientWallet = walletRepository.findByUserId(payment.getFromUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Client wallet not found"));

        // Get or create freelancer wallet
        Wallet freelancerWallet = walletRepository.findByUserId(payment.getToUser().getId())
                .orElse(null);

        if (freelancerWallet == null) {
            // Create wallet for freelancer if doesn't exist
            freelancerWallet = walletRepository.save(Wallet.builder()
                    .user(payment.getToUser())
                    .balance(BigDecimal.ZERO)
                    .escrowBalance(BigDecimal.ZERO)
                    .totalEarned(BigDecimal.ZERO)
                    .currency("USD")
                    .build());
        }

        // Release escrow - MOVE from client escrow to freelancer balance
        String description = String.format("Payment released for project: %s", payment.getProjectId());
        walletService.refundEscrow(clientWallet.getId(), payment.getAmount(), description, payment.getId().toString());

        // Credit net amount to freelancer wallet
        walletService.credit(
                freelancerWallet.getId(),
                payment.getNetAmount(),
                description,
                payment.getId().toString()
        );

        // Update payment status
        payment.setStatus(PaymentStatus.RELEASED);
        payment.setEscrowReleasedAt(LocalDateTime.now());
        payment.setCompletedAt(LocalDateTime.now());
        payment = paymentRepository.save(payment);

        log.info("Payment released successfully. Payment ID: {}, Net amount: {}",
                payment.getId(), payment.getNetAmount());
        return convertToResponse(payment);
    }

    @Override
    @Transactional
    public PaymentResponse refundPayment(UUID paymentId, RefundPaymentRequest request) {
        log.info("Refunding payment: {}", paymentId);

        // Get payment
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        // Validate payment status
        if (payment.getStatus() != PaymentStatus.ESCROW_HOLD) {
            throw new BadRequestException(
                    String.format("Cannot refund payment with status: %s", payment.getStatus())
            );
        }

        // Get client wallet (where escrow is held)
        Wallet clientWallet = walletRepository.findByUserId(payment.getFromUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Client wallet not found"));

        // Refund escrow - MOVE from escrow back to client balance
        String description = String.format("Refund for project: %s. Reason: %s",
                payment.getProjectId(), request.getReason());
        walletService.refundEscrow(
                clientWallet.getId(),
                payment.getAmount(),
                description,
                paymentId.toString()
        );

        // Update payment status
        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setCompletedAt(LocalDateTime.now());
        payment = paymentRepository.save(payment);

        log.info("Payment refunded successfully. Payment ID: {}", paymentId);
        return convertToResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(UUID paymentId) {
        log.info("Fetching payment: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        return convertToResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByProjectId(String projectId) {
        log.info("Fetching payments for project: {}", projectId);

        List<Payment> payments = paymentRepository.findByProjectId(projectId);
        return payments.stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getUserPayments(UUID userId, Pageable pageable) {
        log.info("Fetching payments for user: {}", userId);

        // Get all payments where user is either client or freelancer
        List<Payment> fromPayments = paymentRepository.findByFromUserId(userId);
        List<Payment> toPayments = paymentRepository.findByToUserId(userId);

        // Combine both lists
        List<Payment> allPayments = new java.util.ArrayList<>(fromPayments);
        allPayments.addAll(toPayments);

        // Convert to response list
        List<PaymentResponse> responseList = allPayments.stream()
                .map(this::convertToResponse)
                .toList();

        // Return as Page (simplified - in production use proper pagination)
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), responseList.size());
        List<PaymentResponse> pageContent = responseList.subList(start, end);

        return new org.springframework.data.domain.PageImpl<>(pageContent, pageable, responseList.size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByStatus(PaymentStatus status) {
        log.info("Fetching payments with status: {}", status);

        List<Payment> payments = paymentRepository.findByStatus(status);
        return payments.stream()
                .map(this::convertToResponse)
                .toList();
    }

    // Helper methods
    private PaymentResponse convertToResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .projectId(payment.getProjectId())
                .fromUserId(payment.getFromUser() != null ? payment.getFromUser().getId() : null)
                .toUserId(payment.getToUser() != null ? payment.getToUser().getId() : null)
                .fromUserName(payment.getFromUser() != null ? payment.getFromUser().getFullName() : null)
                .toUserName(payment.getToUser() != null ? payment.getToUser().getFullName() : null)
                .type(payment.getType())
                .status(payment.getStatus())
                .amount(payment.getAmount())
                .fee(payment.getFee())
                .netAmount(payment.getNetAmount())
                .method(payment.getMethod())
                .isEscrow(payment.getIsEscrow())
                .escrowFundedAt(payment.getEscrowFundedAt())
                .escrowReleasedAt(payment.getEscrowReleasedAt())
                .createdAt(payment.getCreatedAt())
                .completedAt(payment.getCompletedAt())
                .build();
    }
}
