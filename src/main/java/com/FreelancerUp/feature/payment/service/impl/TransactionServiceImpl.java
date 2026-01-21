package com.FreelancerUp.feature.payment.service.impl;

import com.FreelancerUp.exception.ResourceNotFoundException;
import com.FreelancerUp.feature.payment.dto.response.TransactionResponse;
import com.FreelancerUp.feature.payment.repository.TransactionRepository;
import com.FreelancerUp.feature.payment.service.TransactionService;
import com.FreelancerUp.feature.payment.service.WalletService;
import com.FreelancerUp.model.entity.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Implementation of TransactionService.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletService walletService;

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(UUID transactionId) {
        log.info("Fetching transaction: {}", transactionId);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        return convertToResponse(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getUserTransactions(UUID userId, Pageable pageable) {
        log.info("Fetching transactions for user: {}", userId);

        Page<Transaction> transactions = transactionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return transactions.map(this::convertToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getWalletTransactions(UUID walletId, Pageable pageable) {
        log.info("Fetching transactions for wallet: {}", walletId);

        // Verify wallet exists
        walletService.getWalletById(walletId);

        Page<Transaction> transactions = transactionRepository.findByWalletIdOrderByCreatedAtDesc(walletId, pageable);
        return transactions.map(this::convertToResponse);
    }

    private TransactionResponse convertToResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .walletId(transaction.getWallet() != null ? transaction.getWallet().getId() : null)
                .userId(transaction.getUser() != null ? transaction.getUser().getId() : null)
                .type(transaction.getType())
                .status(transaction.getStatus())
                .amount(transaction.getAmount())
                .description(transaction.getDescription())
                .referenceId(transaction.getReferenceId())
                .balanceBefore(transaction.getBalanceBefore())
                .balanceAfter(transaction.getBalanceAfter())
                .createdAt(transaction.getCreatedAt())
                .completedAt(transaction.getCompletedAt())
                .build();
    }
}
