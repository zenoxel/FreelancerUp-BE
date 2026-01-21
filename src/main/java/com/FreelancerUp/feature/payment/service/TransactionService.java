package com.FreelancerUp.feature.payment.service;

import com.FreelancerUp.feature.payment.dto.response.TransactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service interface for Transaction operations.
 * Handles transaction history and queries.
 */
public interface TransactionService {

    /**
     * Get transaction by ID.
     *
     * @param transactionId Transaction ID
     * @return TransactionResponse
     */
    TransactionResponse getTransactionById(UUID transactionId);

    /**
     * Get transaction history for user.
     *
     * @param userId User ID
     * @param pageable Pageable
     * @return Page of TransactionResponse
     */
    Page<TransactionResponse> getUserTransactions(UUID userId, Pageable pageable);

    /**
     * Get transaction history for wallet.
     *
     * @param walletId Wallet ID
     * @param pageable Pageable
     * @return Page of TransactionResponse
     */
    Page<TransactionResponse> getWalletTransactions(UUID walletId, Pageable pageable);
}
