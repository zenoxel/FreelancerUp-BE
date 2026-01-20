package com.FreelancerUp;

import com.FreelancerUp.model.entity.*;
import com.FreelancerUp.model.enums.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("dev")
class EntityTest {

    @Test
    void testUserEntityCreation() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .password("encoded_password")
                .fullName("Test User")
                .avatarUrl("https://example.com/avatar.jpg")
                .role(Role.FREELANCER)
                .isActive(true)
                .isEmailVerified(false)
                .build();

        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getRole()).isEqualTo(Role.FREELANCER);
        assertThat(user.getIsActive()).isTrue();
    }

    @Test
    void testClientEntityCreation() {
        Client client = Client.builder()
                .id(UUID.randomUUID())
                .companyName("Tech Corp")
                .industry("Technology")
                .companySize(CompanySize.SIZE_11_50)
                .paymentMethods(List.of())
                .totalSpent(BigDecimal.ZERO)
                .postedProjects(0)
                .build();

        assertThat(client).isNotNull();
        assertThat(client.getCompanyName()).isEqualTo("Tech Corp");
        assertThat(client.getCompanySize()).isEqualTo(CompanySize.SIZE_11_50);
        assertThat(client.getTotalSpent()).isEqualByComparingTo("0");
    }

    @Test
    void testWalletEntityCreation() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("wallet@example.com")
                .password("encoded")
                .build();

        Wallet wallet = Wallet.builder()
                .id(UUID.randomUUID())
                .user(user)
                .balance(BigDecimal.ZERO)
                .escrowBalance(BigDecimal.ZERO)
                .totalEarned(BigDecimal.ZERO)
                .currency("USD")
                .build();

        assertThat(wallet).isNotNull();
        assertThat(wallet.getBalance()).isEqualByComparingTo("0");
        assertThat(wallet.getCurrency()).isEqualTo("USD");
        assertThat(wallet.getUser()).isNotNull();
    }

    @Test
    void testTransactionEntityCreation() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("txn@example.com")
                .build();

        Wallet wallet = Wallet.builder()
                .id(UUID.randomUUID())
                .user(user)
                .build();

        Transaction transaction = Transaction.builder()
                .id(UUID.randomUUID())
                .wallet(wallet)
                .user(user)
                .type(TransactionType.CREDIT)
                .status(TransactionStatus.COMPLETED)
                .amount(new BigDecimal("100.00"))
                .balanceBefore(BigDecimal.ZERO)
                .balanceAfter(new BigDecimal("100.00"))
                .build();

        assertThat(transaction).isNotNull();
        assertThat(transaction.getType()).isEqualTo(TransactionType.CREDIT);
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
        assertThat(transaction.getAmount()).isEqualByComparingTo("100.00");
    }

    @Test
    void testPaymentEntityCreation() {
        User client = User.builder()
                .id(UUID.randomUUID())
                .email("client@example.com")
                .role(Role.CLIENT)
                .build();

        User freelancer = User.builder()
                .id(UUID.randomUUID())
                .email("freelancer@example.com")
                .role(Role.FREELANCER)
                .build();

        Payment payment = Payment.builder()
                .id(UUID.randomUUID())
                .projectId("project-id-123")
                .fromUser(client)
                .toUser(freelancer)
                .type(PaymentType.FINAL)
                .status(PaymentStatus.ESCROW_HOLD)
                .amount(new BigDecimal("1000.00"))
                .fee(new BigDecimal("50.00"))
                .netAmount(new BigDecimal("950.00"))
                .method(PaymentMethodType.WALLET)
                .isEscrow(true)
                .build();

        assertThat(payment).isNotNull();
        assertThat(payment.getType()).isEqualTo(PaymentType.FINAL);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.ESCROW_HOLD);
        assertThat(payment.getIsEscrow()).isTrue();
        assertThat(payment.getNetAmount()).isEqualByComparingTo("950.00");
    }

    @Test
    void testContractEntityCreation() {
        User client = User.builder()
                .id(UUID.randomUUID())
                .email("contract-client@example.com")
                .role(Role.CLIENT)
                .build();

        User freelancer = User.builder()
                .id(UUID.randomUUID())
                .email("contract-freelancer@example.com")
                .role(Role.FREELANCER)
                .build();

        Contract contract = Contract.builder()
                .id(UUID.randomUUID())
                .projectId("project-id-456")
                .client(client)
                .freelancer(freelancer)
                .status(ContractStatus.ACTIVE)
                .build();

        assertThat(contract).isNotNull();
        assertThat(contract.getStatus()).isEqualTo(ContractStatus.ACTIVE);
        assertThat(contract.getProjectId()).isEqualTo("project-id-456");
    }

    @Test
    void testReviewEntityCreation() {
        User fromUser = User.builder()
                .id(UUID.randomUUID())
                .email("reviewer@example.com")
                .build();

        User toUser = User.builder()
                .id(UUID.randomUUID())
                .email("reviewee@example.com")
                .build();

        Review review = Review.builder()
                .id(UUID.randomUUID())
                .projectId("project-id-789")
                .fromUser(fromUser)
                .toUser(toUser)
                .rating(5)
                .comment("Great work!")
                .communicationRating(5)
                .qualityRating(5)
                .timelineRating(5)
                .professionalismRating(5)
                .responsivenessRating(5)
                .isVisible(true)
                .build();

        assertThat(review).isNotNull();
        assertThat(review.getRating()).isEqualTo(5);
        assertThat(review.getComment()).isEqualTo("Great work!");
        assertThat(review.getIsVisible()).isTrue();
    }

    @Test
    void testConversationEntityCreation() {
        Conversation conversation = Conversation.builder()
                .id(UUID.randomUUID())
                .projectId("project-id-999")
                .participantIds(List.of(
                    UUID.randomUUID(),
                    UUID.randomUUID()
                ))
                .isActive(true)
                .build();

        assertThat(conversation).isNotNull();
        assertThat(conversation.getProjectId()).isEqualTo("project-id-999");
        assertThat(conversation.getParticipantIds()).hasSize(2);
        assertThat(conversation.getIsActive()).isTrue();
    }
}
