package com.FreelancerUp.feature.contract.service;

import com.FreelancerUp.feature.contract.dto.response.ContractResponse;
import com.FreelancerUp.model.enums.ContractStatus;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for Contract operations.
 * Handles contract creation, status updates, and retrieval.
 */
public interface ContractService {

    /**
     * Create a new contract for a project when a bid is accepted.
     * This is typically called automatically when a client accepts a bid.
     *
     * @param projectId Project ID (MongoDB)
     * @param clientId Client User ID
     * @param freelancerId Freelancer User ID
     * @return ContractResponse
     */
    ContractResponse createContract(String projectId, UUID clientId, UUID freelancerId);

    /**
     * Get contract by ID.
     *
     * @param contractId Contract ID
     * @return ContractResponse
     */
    ContractResponse getContractById(UUID contractId);

    /**
     * Get contract by project ID.
     * Each project should have at most one active contract.
     *
     * @param projectId Project ID (MongoDB)
     * @return ContractResponse
     */
    ContractResponse getContractByProjectId(String projectId);

    /**
     * Get all contracts for a client.
     *
     * @param clientId Client User ID
     * @return List of ContractResponse
     */
    List<ContractResponse> getContractsByClient(UUID clientId);

    /**
     * Get all contracts for a freelancer.
     *
     * @param freelancerId Freelancer User ID
     * @return List of ContractResponse
     */
    List<ContractResponse> getContractsByFreelancer(UUID freelancerId);

    /**
     * Get all contracts for a user (as client or freelancer).
     *
     * @param userId User ID
     * @return List of ContractResponse
     */
    List<ContractResponse> getContractsByUser(UUID userId);

    /**
     * Update contract status.
     *
     * @param contractId Contract ID
     * @param status New status
     * @return ContractResponse
     */
    ContractResponse updateContractStatus(UUID contractId, ContractStatus status);

    /**
     * Complete a contract.
     * This is called when a project is marked as completed.
     *
     * @param contractId Contract ID
     * @return ContractResponse
     */
    ContractResponse completeContract(UUID contractId);
}
