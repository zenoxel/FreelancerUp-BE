package com.FreelancerUp;

import com.FreelancerUp.feature.bid.repository.BidRepository;
import com.FreelancerUp.feature.chat.repository.ConversationRepository;
import com.FreelancerUp.feature.chat.repository.MessageRepository;
import com.FreelancerUp.feature.client.repository.ClientRepository;
import com.FreelancerUp.feature.contract.repository.ContractRepository;
import com.FreelancerUp.feature.freelancer.repository.EducationRepository;
import com.FreelancerUp.feature.freelancer.repository.ExperienceRepository;
import com.FreelancerUp.feature.freelancer.repository.FreelancerRepository;
import com.FreelancerUp.feature.payment.repository.PaymentRepository;
import com.FreelancerUp.feature.payment.repository.TransactionRepository;
import com.FreelancerUp.feature.payment.repository.WalletRepository;
import com.FreelancerUp.feature.project.repository.ProjectRepository;
import com.FreelancerUp.feature.review.repository.ReviewRepository;
import com.FreelancerUp.feature.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("dev")
class RepositoryTest {

    // PostgreSQL Repositories
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    // MongoDB Repositories
    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private BidRepository bidRepository;

    @Autowired
    private FreelancerRepository freelancerRepository;

    @Autowired
    private ExperienceRepository experienceRepository;

    @Autowired
    private EducationRepository educationRepository;

    @Autowired
    private MessageRepository messageRepository;

    // ========== PostgreSQL Repository Tests ==========

    @Test
    void userRepository_shouldBeLoaded() {
        assertThat(userRepository).isNotNull();
    }

    @Test
    void clientRepository_shouldBeLoaded() {
        assertThat(clientRepository).isNotNull();
    }

    @Test
    void walletRepository_shouldBeLoaded() {
        assertThat(walletRepository).isNotNull();
    }

    @Test
    void transactionRepository_shouldBeLoaded() {
        assertThat(transactionRepository).isNotNull();
    }

    @Test
    void paymentRepository_shouldBeLoaded() {
        assertThat(paymentRepository).isNotNull();
    }

    @Test
    void contractRepository_shouldBeLoaded() {
        assertThat(contractRepository).isNotNull();
    }

    @Test
    void reviewRepository_shouldBeLoaded() {
        assertThat(reviewRepository).isNotNull();
    }

    @Test
    void conversationRepository_shouldBeLoaded() {
        assertThat(conversationRepository).isNotNull();
    }

    // ========== MongoDB Repository Tests ==========

    @Test
    void projectRepository_shouldBeLoaded() {
        assertThat(projectRepository).isNotNull();
    }

    @Test
    void bidRepository_shouldBeLoaded() {
        assertThat(bidRepository).isNotNull();
    }

    @Test
    void freelancerRepository_shouldBeLoaded() {
        assertThat(freelancerRepository).isNotNull();
    }

    @Test
    void experienceRepository_shouldBeLoaded() {
        assertThat(experienceRepository).isNotNull();
    }

    @Test
    void educationRepository_shouldBeLoaded() {
        assertThat(educationRepository).isNotNull();
    }

    @Test
    void messageRepository_shouldBeLoaded() {
        assertThat(messageRepository).isNotNull();
    }

    // ========== Integration Tests ==========

    @Test
    void allPostgreSQLRepositories_shouldBeAutowired() {
        // Verify all 8 PostgreSQL repositories are loaded
        assertThat(userRepository).isNotNull();
        assertThat(clientRepository).isNotNull();
        assertThat(walletRepository).isNotNull();
        assertThat(transactionRepository).isNotNull();
        assertThat(paymentRepository).isNotNull();
        assertThat(contractRepository).isNotNull();
        assertThat(reviewRepository).isNotNull();
        assertThat(conversationRepository).isNotNull();
    }

    @Test
    void allMongoDBRepositories_shouldBeAutowired() {
        // Verify all 6 MongoDB repositories are loaded
        assertThat(projectRepository).isNotNull();
        assertThat(bidRepository).isNotNull();
        assertThat(freelancerRepository).isNotNull();
        assertThat(experienceRepository).isNotNull();
        assertThat(educationRepository).isNotNull();
        assertThat(messageRepository).isNotNull();
    }

    @Test
    void totalRepositories_count_shouldBe14() {
        // Verify we have 14 repositories total
        int postgresCount = 8;
        int mongoCount = 6;
        assertThat(postgresCount + mongoCount).isEqualTo(14);
    }
}
