package com.FreelancerUp;

import com.FreelancerUp.exception.BadRequestException;
import com.FreelancerUp.exception.ResourceNotFoundException;
import com.FreelancerUp.feature.contract.dto.response.ContractResponse;
import com.FreelancerUp.feature.contract.repository.ContractRepository;
import com.FreelancerUp.feature.contract.service.impl.ContractServiceImpl;
import com.FreelancerUp.feature.project.repository.ProjectRepository;
import com.FreelancerUp.feature.user.repository.UserRepository;
import com.FreelancerUp.model.entity.Contract;
import com.FreelancerUp.model.entity.User;
import com.FreelancerUp.model.enums.ContractStatus;
import com.FreelancerUp.model.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ContractService.
 *
 * Tests the contract lifecycle:
 * 1. Create contract when bid is accepted
 * 2. Retrieve contract by ID and project
 * 3. Update contract status
 * 4. Complete contract
 * 5. Get contracts by user
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Contract Service Tests")
class ContractServiceTest {

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ContractServiceImpl contractService;

    private UUID clientId;
    private UUID freelancerId;
    private String projectId;
    private UUID contractId;
    private User clientUser;
    private User freelancerUser;
    private Contract contract;

    @BeforeEach
    void setUp() {
        clientId = UUID.randomUUID();
        freelancerId = UUID.randomUUID();
        projectId = "507f1f77bcf86cd799439011";
        contractId = UUID.randomUUID();

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

        // Setup contract
        contract = Contract.builder()
                .id(contractId)
                .projectId(projectId)
                .client(clientUser)
                .freelancer(freelancerUser)
                .status(ContractStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should create contract successfully")
    void testCreateContract_Success() {
        // Arrange
        when(contractRepository.findByProjectId(projectId)).thenReturn(Optional.empty());
        when(userRepository.findById(clientId)).thenReturn(Optional.of(clientUser));
        when(userRepository.findById(freelancerId)).thenReturn(Optional.of(freelancerUser));
        when(projectRepository.existsById(projectId)).thenReturn(true);
        when(contractRepository.save(any(Contract.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ContractResponse response = contractService.createContract(projectId, clientId, freelancerId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getProjectId()).isEqualTo(projectId);
        assertThat(response.getClientId()).isEqualTo(clientId);
        assertThat(response.getFreelancerId()).isEqualTo(freelancerId);
        assertThat(response.getStatus()).isEqualTo(ContractStatus.ACTIVE);

        verify(contractRepository).save(any(Contract.class));
    }

    @Test
    @DisplayName("Should fail to create contract when one already exists for project")
    void testCreateContract_ContractAlreadyExists() {
        // Arrange
        when(contractRepository.findByProjectId(projectId)).thenReturn(Optional.of(contract));

        // Act & Assert
        assertThatThrownBy(() -> contractService.createContract(projectId, clientId, freelancerId))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already exists");

        verify(contractRepository, never()).save(any(Contract.class));
    }

    @Test
    @DisplayName("Should fail to create contract when client not found")
    void testCreateContract_ClientNotFound() {
        // Arrange
        when(contractRepository.findByProjectId(projectId)).thenReturn(Optional.empty());
        when(userRepository.findById(clientId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> contractService.createContract(projectId, clientId, freelancerId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Client not found");
    }

    @Test
    @DisplayName("Should get contract by ID successfully")
    void testGetContractById_Success() {
        // Arrange
        when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));

        // Act
        ContractResponse response = contractService.getContractById(contractId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(contractId);
        assertThat(response.getProjectId()).isEqualTo(projectId);
    }

    @Test
    @DisplayName("Should throw exception when contract not found by ID")
    void testGetContractById_NotFound() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(contractRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> contractService.getContractById(nonExistentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Contract not found");
    }

    @Test
    @DisplayName("Should get contract by project ID successfully")
    void testGetContractByProjectId_Success() {
        // Arrange
        when(contractRepository.findByProjectId(projectId)).thenReturn(Optional.of(contract));

        // Act
        ContractResponse response = contractService.getContractByProjectId(projectId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getProjectId()).isEqualTo(projectId);
    }

    @Test
    @DisplayName("Should get contracts by client")
    void testGetContractsByClient() {
        // Arrange
        when(contractRepository.findByClientId(clientId)).thenReturn(List.of(contract));

        // Act
        List<ContractResponse> responses = contractService.getContractsByClient(clientId);

        // Assert
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getClientId()).isEqualTo(clientId);
    }

    @Test
    @DisplayName("Should get contracts by freelancer")
    void testGetContractsByFreelancer() {
        // Arrange
        when(contractRepository.findByFreelancerId(freelancerId)).thenReturn(List.of(contract));

        // Act
        List<ContractResponse> responses = contractService.getContractsByFreelancer(freelancerId);

        // Assert
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getFreelancerId()).isEqualTo(freelancerId);
    }

    @Test
    @DisplayName("Should get contracts by user (as client or freelancer)")
    void testGetContractsByUser() {
        // Arrange
        when(contractRepository.findAllByUser(clientId)).thenReturn(List.of(contract));

        // Act
        List<ContractResponse> responses = contractService.getContractsByUser(clientId);

        // Assert
        assertThat(responses).hasSize(1);
    }

    @Test
    @DisplayName("Should update contract status successfully")
    void testUpdateContractStatus_Success() {
        // Arrange
        when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));
        when(contractRepository.save(any(Contract.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ContractResponse response = contractService.updateContractStatus(contractId, ContractStatus.COMPLETED);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(ContractStatus.COMPLETED);

        verify(contractRepository).save(any(Contract.class));
    }

    @Test
    @DisplayName("Should fail to update contract status when transition is invalid")
    void testUpdateContractStatus_InvalidTransition() {
        // Arrange
        contract.setStatus(ContractStatus.ACTIVE);
        when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));

        // Act & Assert - cannot transition from ACTIVE to ACTIVE
        assertThatThrownBy(() -> contractService.updateContractStatus(contractId, ContractStatus.ACTIVE))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cannot transition");
    }

    @Test
    @DisplayName("Should fail to update completed contract")
    void testUpdateContractStatus_AlreadyCompleted() {
        // Arrange
        contract.setStatus(ContractStatus.COMPLETED);
        when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));

        // Act & Assert
        assertThatThrownBy(() -> contractService.updateContractStatus(contractId, ContractStatus.ACTIVE))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cannot modify a completed contract");
    }

    @Test
    @DisplayName("Should complete contract successfully")
    void testCompleteContract_Success() {
        // Arrange
        when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));
        when(contractRepository.save(any(Contract.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ContractResponse response = contractService.completeContract(contractId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(ContractStatus.COMPLETED);

        verify(contractRepository).save(any(Contract.class));
    }
}
