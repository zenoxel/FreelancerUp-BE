package com.FreelancerUp.feature.contract.service.impl;

import com.FreelancerUp.exception.BadRequestException;
import com.FreelancerUp.exception.ResourceNotFoundException;
import com.FreelancerUp.feature.contract.dto.response.ContractResponse;
import com.FreelancerUp.feature.contract.repository.ContractRepository;
import com.FreelancerUp.feature.contract.service.ContractService;
import com.FreelancerUp.feature.project.repository.ProjectRepository;
import com.FreelancerUp.feature.user.repository.UserRepository;
import com.FreelancerUp.model.entity.Contract;
import com.FreelancerUp.model.entity.User;
import com.FreelancerUp.model.enums.ContractStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of ContractService.
 *
 * Key Features:
 * - Auto-create contract when bid is accepted
 * - Track contract status (ACTIVE, COMPLETED)
 * - Link contract to project
 * - Update freelancer assignment
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {

    private final ContractRepository contractRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    @Override
    @Transactional
    public ContractResponse createContract(String projectId, UUID clientId, UUID freelancerId) {
        log.info("Creating contract for project: {} between client: {} and freelancer: {}",
                projectId, clientId, freelancerId);

        // Check if contract already exists for this project
        contractRepository.findByProjectId(projectId).ifPresent(existing -> {
            throw new BadRequestException("Contract already exists for this project");
        });

        // Validate users exist
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

        User freelancer = userRepository.findById(freelancerId)
                .orElseThrow(() -> new ResourceNotFoundException("Freelancer not found"));

        // Validate project exists
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found");
        }

        // Create contract
        Contract contract = Contract.builder()
                .projectId(projectId)
                .client(client)
                .freelancer(freelancer)
                .status(ContractStatus.ACTIVE)
                .build();

        contract = contractRepository.save(contract);

        log.info("Contract created successfully: {}", contract.getId());
        return convertToResponse(contract);
    }

    @Override
    @Transactional(readOnly = true)
    public ContractResponse getContractById(UUID contractId) {
        log.info("Fetching contract: {}", contractId);

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found"));

        return convertToResponse(contract);
    }

    @Override
    @Transactional(readOnly = true)
    public ContractResponse getContractByProjectId(String projectId) {
        log.info("Fetching contract for project: {}", projectId);

        Contract contract = contractRepository.findByProjectId(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found for this project"));

        return convertToResponse(contract);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractResponse> getContractsByClient(UUID clientId) {
        log.info("Fetching contracts for client: {}", clientId);

        List<Contract> contracts = contractRepository.findByClientId(clientId);
        return contracts.stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractResponse> getContractsByFreelancer(UUID freelancerId) {
        log.info("Fetching contracts for freelancer: {}", freelancerId);

        List<Contract> contracts = contractRepository.findByFreelancerId(freelancerId);
        return contracts.stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractResponse> getContractsByUser(UUID userId) {
        log.info("Fetching contracts for user: {}", userId);

        List<Contract> contracts = contractRepository.findAllByUser(userId);
        return contracts.stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    @Transactional
    public ContractResponse updateContractStatus(UUID contractId, ContractStatus status) {
        log.info("Updating contract: {} status to: {}", contractId, status);

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found"));

        // Validate status transition
        validateStatusTransition(contract.getStatus(), status);

        contract.setStatus(status);
        contract = contractRepository.save(contract);

        log.info("Contract status updated successfully: {}", contractId);
        return convertToResponse(contract);
    }

    @Override
    @Transactional
    public ContractResponse completeContract(UUID contractId) {
        log.info("Completing contract: {}", contractId);

        return updateContractStatus(contractId, ContractStatus.COMPLETED);
    }

    // Helper methods

    /**
     * Validate contract status transitions.
     * Allowed transitions:
     * - ACTIVE -> COMPLETED
     *
     * @param from Current status
     * @param to Target status
     * @throws BadRequestException if transition is not allowed
     */
    private void validateStatusTransition(ContractStatus from, ContractStatus to) {
        if (from == ContractStatus.COMPLETED) {
            throw new BadRequestException("Cannot modify a completed contract");
        }

        // Only allow ACTIVE -> COMPLETED
        if (from == ContractStatus.ACTIVE && to != ContractStatus.COMPLETED) {
            throw new BadRequestException(
                    String.format("Cannot transition from %s to %s", from, to)
            );
        }
    }

    private ContractResponse convertToResponse(Contract contract) {
        return ContractResponse.builder()
                .id(contract.getId())
                .projectId(contract.getProjectId())
                .clientId(contract.getClient() != null ? contract.getClient().getId() : null)
                .clientName(contract.getClient() != null ? contract.getClient().getFullName() : null)
                .clientEmail(contract.getClient() != null ? contract.getClient().getEmail() : null)
                .freelancerId(contract.getFreelancer() != null ? contract.getFreelancer().getId() : null)
                .freelancerName(contract.getFreelancer() != null ? contract.getFreelancer().getFullName() : null)
                .freelancerEmail(contract.getFreelancer() != null ? contract.getFreelancer().getEmail() : null)
                .status(contract.getStatus())
                .createdAt(contract.getCreatedAt())
                .updatedAt(contract.getUpdatedAt())
                .build();
    }
}
