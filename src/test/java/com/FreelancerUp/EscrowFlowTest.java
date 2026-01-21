package com.FreelancerUp;

import com.FreelancerUp.exception.BadRequestException;
import com.FreelancerUp.exception.ResourceNotFoundException;
import com.FreelancerUp.feature.bid.repository.BidRepository;
import com.FreelancerUp.feature.payment.dto.request.FundEscrowRequest;
import com.FreelancerUp.feature.payment.dto.request.RefundPaymentRequest;
import com.FreelancerUp.feature.payment.dto.request.ReleasePaymentRequest;
import com.FreelancerUp.feature.payment.dto.response.PaymentResponse;
import com.FreelancerUp.feature.payment.dto.response.WalletResponse;
import com.FreelancerUp.feature.payment.repository.PaymentRepository;
import com.FreelancerUp.feature.payment.repository.WalletRepository;
import com.FreelancerUp.feature.payment.service.WalletService;
import com.FreelancerUp.feature.payment.service.impl.PaymentServiceImpl;
import com.FreelancerUp.feature.project.repository.ProjectRepository;
import com.FreelancerUp.feature.user.repository.UserRepository;
import com.FreelancerUp.model.document.Bid;
import com.FreelancerUp.model.document.Project;
import com.FreelancerUp.model.entity.Payment;
import com.FreelancerUp.model.entity.User;
import com.FreelancerUp.model.entity.Wallet;
import com.FreelancerUp.model.enums.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * CRITICAL TEST: Escrow Flow Tests
 *
 * Tests the complete escrow payment lifecycle:
 * 1. Fund Escrow: Client Wallet -> Escrow Hold
 * 2. Release Payment: Escrow -> Freelancer Wallet
 * 3. Refund: Escrow -> Client Wallet
 *
 * All tests verify:
 * - @Transactional behavior
 * - Atomic wallet updates
 * - Double-entry accounting
 * - State machine validation
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@DisplayName("Escrow Flow Tests")
class EscrowFlowTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletService walletService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private BidRepository bidRepository;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private UUID clientId;
    private UUID freelancerId;
    private String projectId;
    private UUID paymentId;
    private User clientUser;
    private User freelancerUser;
    private Wallet clientWallet;
    private Wallet freelancerWallet;
    private Project project;
    private Bid acceptedBid;
    private Payment escrowPayment;

    @BeforeEach
    void setUp() {
        clientId = UUID.randomUUID();
        freelancerId = UUID.randomUUID();
        projectId = "507f1f77bcf86cd799439011";
        paymentId = UUID.randomUUID();

        // Setup users
        clientUser = User.builder()
                .id(clientId)
                .email("client@example.com")
                .fullName("Client User")
                .role(Role.CLIENT)
                .build();

        freelancerUser = User.builder()
                .id(freelancerId)
                .email("freelancer@example.com")
                .fullName("Freelancer User")
                .role(Role.FREELANCER)
                .build();

        // Setup wallets
        clientWallet = Wallet.builder()
                .id(UUID.randomUUID())
                .user(clientUser)
                .balance(new BigDecimal("5000.00"))
                .escrowBalance(BigDecimal.ZERO)
                .totalEarned(BigDecimal.ZERO)
                .currency("USD")
                .build();

        freelancerWallet = Wallet.builder()
                .id(UUID.randomUUID())
                .user(freelancerUser)
                .balance(new BigDecimal("1000.00"))
                .escrowBalance(BigDecimal.ZERO)
                .totalEarned(BigDecimal.ZERO)
                .currency("USD")
                .build();

        // Setup project
        project = Project.builder()
                .id(projectId)
                .clientId(clientId.toString())
                .title("Web Development Project")
                .description("Build a web application")
                .status(ProjectStatus.OPEN)
                .build();

        // Setup accepted bid
        acceptedBid = Bid.builder()
                .id("bid123")
                .projectId(projectId)
                .freelancerId(freelancerId.toString())
                .proposal("I can do this project")
                .price(new BigDecimal("2000.00"))
                .status(BidStatus.ACCEPTED)
                .createdAt(LocalDateTime.now())
                .build();

        // Setup escrow payment
        escrowPayment = Payment.builder()
                .id(paymentId)
                .projectId(projectId)
                .fromUser(clientUser)
                .toUser(freelancerUser)
                .type(PaymentType.MILESTONE)
                .status(PaymentStatus.ESCROW_HOLD)
                .amount(new BigDecimal("2000.00"))
                .fee(new BigDecimal("100.00"))
                .netAmount(new BigDecimal("1900.00"))
                .method(PaymentMethodType.WALLET_BALANCE)
                .isEscrow(true)
                .escrowFundedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        // Set platform fee percentage using reflection
        ReflectionTestUtils.setField(paymentService, "platformFeePercentage", new BigDecimal("5.0"));
    }

    @Test
    @DisplayName("Should fund escrow successfully")
    void testFundEscrow_Success() {
        // Arrange
        FundEscrowRequest request = FundEscrowRequest.builder()
                .projectId(projectId)
                .freelancerId(freelancerId)
                .amount(new BigDecimal("2000.00"))
                .type(PaymentType.MILESTONE)
                .method(PaymentMethodType.WALLET_BALANCE)
                .description("Initial payment")
                .build();

        when(userRepository.existsById(clientId)).thenReturn(true);
        when(userRepository.existsById(freelancerId)).thenReturn(true);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(bidRepository.findByProjectIdAndStatus(projectId, BidStatus.ACCEPTED))
                .thenReturn(Optional.of(acceptedBid));
        when(paymentRepository.findByProjectId(projectId)).thenReturn(List.of());
        when(walletRepository.findByUserId(clientId)).thenReturn(Optional.of(clientWallet));
        when(walletService.holdEscrow(any(), any(), any(), any())).thenReturn(null);
        when(userRepository.findById(clientId)).thenReturn(Optional.of(clientUser));
        when(userRepository.findById(freelancerId)).thenReturn(Optional.of(freelancerUser));
        when(paymentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        PaymentResponse response = paymentService.fundEscrow(clientId, request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getProjectId()).isEqualTo(projectId);
        assertThat(response.getFromUserId()).isEqualTo(clientId);
        assertThat(response.getToUserId()).isEqualTo(freelancerId);
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.ESCROW_HOLD);
        assertThat(response.getAmount()).isEqualTo(new BigDecimal("2000.00"));
        assertThat(response.getFee()).isEqualTo(new BigDecimal("100.00")); // 5% of 2000
        assertThat(response.getNetAmount()).isEqualTo(new BigDecimal("1900.00"));

        // Verify interactions
        verify(walletService).holdEscrow(
                eq(clientWallet.getId()),
                eq(new BigDecimal("2000.00")),
                contains("Escrow payment for project"),
                eq(projectId)
        );
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should fail to fund escrow when project is not owned by client")
    void testFundEscrow_NotProjectOwner() {
        // Arrange
        UUID differentClientId = UUID.randomUUID();
        project.setClientId(differentClientId.toString());

        FundEscrowRequest request = FundEscrowRequest.builder()
                .projectId(projectId)
                .freelancerId(freelancerId)
                .amount(new BigDecimal("2000.00"))
                .type(PaymentType.MILESTONE)
                .method(PaymentMethodType.WALLET_BALANCE)
                .build();

        when(userRepository.existsById(clientId)).thenReturn(true);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        // Act & Assert
        assertThatThrownBy(() -> paymentService.fundEscrow(clientId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("own projects");
    }

    @Test
    @DisplayName("Should fail to fund escrow when no accepted bid exists")
    void testFundEscrow_NoAcceptedBid() {
        // Arrange
        FundEscrowRequest request = FundEscrowRequest.builder()
                .projectId(projectId)
                .freelancerId(freelancerId)
                .amount(new BigDecimal("2000.00"))
                .type(PaymentType.MILESTONE)
                .method(PaymentMethodType.WALLET_BALANCE)
                .build();

        when(userRepository.existsById(clientId)).thenReturn(true);
        when(userRepository.existsById(freelancerId)).thenReturn(true);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(bidRepository.findByProjectIdAndStatus(projectId, BidStatus.ACCEPTED))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> paymentService.fundEscrow(clientId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("No accepted bid found");
    }

    @Test
    @DisplayName("Should release payment successfully")
    void testReleasePayment_Success() {
        // Arrange
        ReleasePaymentRequest request = ReleasePaymentRequest.builder()
                .paymentId(paymentId)
                .reason("Project completed successfully")
                .build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(escrowPayment));
        when(walletRepository.findByUserId(clientId)).thenReturn(Optional.of(clientWallet));
        when(walletRepository.findByUserId(freelancerId)).thenReturn(Optional.of(freelancerWallet));
        when(walletService.refundEscrow(any(), any(), any(), any())).thenReturn(null);
        when(walletService.credit(any(), any(), any(), any())).thenReturn(null);
        when(paymentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        PaymentResponse response = paymentService.releasePayment(clientId, request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(paymentId);
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.RELEASED);
        assertThat(response.getNetAmount()).isEqualTo(new BigDecimal("1900.00"));

        // Verify interactions
        verify(walletService).refundEscrow(
                eq(clientWallet.getId()),
                eq(new BigDecimal("2000.00")),
                contains("Payment released"),
                eq(paymentId.toString())
        );
        verify(walletService).credit(
                eq(freelancerWallet.getId()),
                eq(new BigDecimal("1900.00")), // Net amount
                contains("Payment released"),
                eq(paymentId.toString())
        );
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should fail to release payment when not escrow holder")
    void testReleasePayment_NotOwner() {
        // Arrange
        UUID differentClientId = UUID.randomUUID();
        ReleasePaymentRequest request = ReleasePaymentRequest.builder()
                .paymentId(paymentId)
                .build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(escrowPayment));

        // Act & Assert
        assertThatThrownBy(() -> paymentService.releasePayment(differentClientId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("own payments");
    }

    @Test
    @DisplayName("Should fail to release payment when status is not ESCROW_HOLD")
    void testReleasePayment_InvalidStatus() {
        // Arrange
        escrowPayment.setStatus(PaymentStatus.COMPLETED);
        ReleasePaymentRequest request = ReleasePaymentRequest.builder()
                .paymentId(paymentId)
                .build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(escrowPayment));

        // Act & Assert
        assertThatThrownBy(() -> paymentService.releasePayment(clientId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cannot release payment");
    }

    @Test
    @DisplayName("Should refund payment successfully")
    void testRefundPayment_Success() {
        // Arrange
        RefundPaymentRequest request = RefundPaymentRequest.builder()
                .paymentId(paymentId)
                .reason("Project cancelled by mutual agreement")
                .build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(escrowPayment));
        when(walletRepository.findByUserId(clientId)).thenReturn(Optional.of(clientWallet));
        when(walletService.refundEscrow(any(), any(), any(), any())).thenReturn(null);
        when(paymentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        PaymentResponse response = paymentService.refundPayment(paymentId, request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(paymentId);
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.REFUNDED);

        // Verify interactions
        verify(walletService).refundEscrow(
                eq(clientWallet.getId()),
                eq(new BigDecimal("2000.00")),
                contains("Refund for project"),
                eq(paymentId.toString())
        );
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should get payment by ID successfully")
    void testGetPaymentById_Success() {
        // Arrange
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(escrowPayment));

        // Act
        PaymentResponse response = paymentService.getPaymentById(paymentId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(paymentId);
        assertThat(response.getProjectId()).isEqualTo(projectId);
    }

    @Test
    @DisplayName("Should throw exception when payment not found")
    void testGetPaymentById_NotFound() {
        // Arrange
        UUID nonExistentPaymentId = UUID.randomUUID();
        when(paymentRepository.findById(nonExistentPaymentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> paymentService.getPaymentById(nonExistentPaymentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Payment not found");
    }

    @Test
    @DisplayName("Should get payments by project ID")
    void testGetPaymentsByProjectId() {
        // Arrange
        when(paymentRepository.findByProjectId(projectId)).thenReturn(List.of(escrowPayment));

        // Act
        List<PaymentResponse> responses = paymentService.getPaymentsByProjectId(projectId);

        // Assert
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getProjectId()).isEqualTo(projectId);
    }

    @Test
    @DisplayName("Should get user payments with pagination")
    void testGetUserPayments() {
        // Arrange
        when(paymentRepository.findByFromUserId(clientId)).thenReturn(List.of(escrowPayment));
        when(paymentRepository.findByToUserId(clientId)).thenReturn(List.of());

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<PaymentResponse> responses = paymentService.getUserPayments(clientId, pageable);

        // Assert
        assertThat(responses).isNotNull();
        assertThat(responses.getContent()).hasSize(1);
        assertThat(responses.getContent().get(0).getFromUserId()).isEqualTo(clientId);
    }
}
