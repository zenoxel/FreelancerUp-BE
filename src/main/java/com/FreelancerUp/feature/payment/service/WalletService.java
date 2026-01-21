package com.FreelancerUp.feature.payment.service;

import com.FreelancerUp.feature.payment.dto.response.TransactionResponse;
import com.FreelancerUp.feature.payment.dto.response.WalletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Service interface for Wallet operations.
 * Handles wallet balance management and transaction history.
 */
public interface WalletService {

    /**
     * Get wallet by user ID.
     *
     * @param userId User ID
     * @return WalletResponse
     */
    WalletResponse getWalletByUserId(UUID userId);

    /**
     * Get wallet by wallet ID.
     *
     * @param walletId Wallet ID
     * @return WalletResponse
     */
    WalletResponse getWalletById(UUID walletId);

    /**
     * Create wallet for user if not exists.
     *
     * @param userId User ID
     * @return WalletResponse
     */
    WalletResponse createWalletForUser(UUID userId);

    /**
     * Get transaction history for wallet.
     *
     * @param walletId Wallet ID
     * @param pageable Pageable
     * @return Page of TransactionResponse
     */
    Page<TransactionResponse> getWalletTransactions(UUID walletId, Pageable pageable);

    /**
     * Get transaction history for user.
     *
     * @param userId User ID
     * @param pageable Pageable
     * @return Page of TransactionResponse
     */
    Page<TransactionResponse> getUserTransactions(UUID userId, Pageable pageable);

    /**
     * Credit amount to wallet (internal use).
     * MUST be called within @Transactional context.
     *
     * @param walletId Wallet ID
     * @param amount Amount to credit
     * @param description Transaction description
     * @param referenceId Reference ID (optional)
     * @return TransactionResponse
     */
    TransactionResponse credit(UUID walletId, BigDecimal amount, String description, String referenceId);

    /**
     * Debit amount from wallet (internal use).
     * MUST be called within @Transactional context.
     *
     * @param walletId Wallet ID
     * @param amount Amount to debit
     * @param description Transaction description
     * @param referenceId Reference ID (optional)
     * @return TransactionResponse
     * @throws IllegalArgumentException if insufficient balance
     */
    TransactionResponse debit(UUID walletId, BigDecimal amount, String description, String referenceId);

    /**
     * Hold amount in escrow (internal use).
     * MUST be called within @Transactional context.
     *
     * @param walletId Wallet ID
     * @param amount Amount to hold
     * @param description Transaction description
     * @param referenceId Reference ID (optional)
     * @return TransactionResponse
     * @throws IllegalArgumentException if insufficient balance
     */
    TransactionResponse holdEscrow(UUID walletId, BigDecimal amount, String description, String referenceId);

    /**
     * Release amount from escrow (internal use).
     * MUST be called within @Transactional context.
     *
     * @param walletId Wallet ID
     * @param amount Amount to release
     * @param description Transaction description
     * @param referenceId Reference ID (optional)
     * @return TransactionResponse
     */
    TransactionResponse releaseEscrow(UUID walletId, BigDecimal amount, String description, String referenceId);

    /**
     * Refund amount from escrow back to balance (internal use).
     * MUST be called within @Transactional context.
     *
     * @param walletId Wallet ID
     * @param amount Amount to refund
     * @param description Transaction description
     * @param referenceId Reference ID (optional)
     * @return TransactionResponse
     */
    TransactionResponse refundEscrow(UUID walletId, BigDecimal amount, String description, String referenceId);

    /**
     * Check if wallet has sufficient balance.
     *
     * @param walletId Wallet ID
     * @param amount Amount to check
     * @return true if sufficient balance
     */
    boolean hasSufficientBalance(UUID walletId, BigDecimal amount);

    /**
     * Check if wallet has sufficient escrow balance.
     *
     * @param walletId Wallet ID
     * @param amount Amount to check
     * @return true if sufficient escrow balance
     */
    boolean hasSufficientEscrowBalance(UUID walletId, BigDecimal amount);
}
