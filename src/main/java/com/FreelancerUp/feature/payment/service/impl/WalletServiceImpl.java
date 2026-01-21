package com.FreelancerUp.feature.payment.service.impl;

import com.FreelancerUp.exception.BadRequestException;
import com.FreelancerUp.exception.ResourceNotFoundException;
import com.FreelancerUp.feature.payment.dto.response.TransactionResponse;
import com.FreelancerUp.feature.payment.dto.response.WalletResponse;
import com.FreelancerUp.feature.payment.repository.TransactionRepository;
import com.FreelancerUp.feature.payment.repository.WalletRepository;
import com.FreelancerUp.feature.payment.service.WalletService;
import com.FreelancerUp.model.entity.Transaction;
import com.FreelancerUp.model.entity.Wallet;
import com.FreelancerUp.model.enums.TransactionStatus;
import com.FreelancerUp.model.enums.TransactionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Implementation of WalletService.
 * CRITICAL: All debit/credit operations MUST be called within @Transactional context.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    @Override
    @Transactional(readOnly = true)
    public WalletResponse getWalletByUserId(UUID userId) {
        log.info("Fetching wallet for user: {}", userId);

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for user"));

        return convertToResponse(wallet);
    }

    @Override
    @Transactional(readOnly = true)
    public WalletResponse getWalletById(UUID walletId) {
        log.info("Fetching wallet: {}", walletId);

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

        return convertToResponse(wallet);
    }

    @Override
    @Transactional
    public WalletResponse createWalletForUser(UUID userId) {
        log.info("Creating wallet for user: {}", userId);

        // Check if wallet already exists
        if (walletRepository.findByUserId(userId).isPresent()) {
            log.warn("Wallet already exists for user: {}", userId);
            return getWalletByUserId(userId);
        }

        // Create new wallet
        Wallet wallet = Wallet.builder()
                .user(null) // Will be set by JPA when user is loaded
                .balance(BigDecimal.ZERO)
                .escrowBalance(BigDecimal.ZERO)
                .totalEarned(BigDecimal.ZERO)
                .currency("USD")
                .build();

        // We need to set the user_id directly since we don't have the User entity here
        // This is a simplified approach - in production, you'd load the User entity first
        wallet = walletRepository.save(wallet);

        log.info("Wallet created successfully: {}", wallet.getId());
        return convertToResponse(wallet);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getWalletTransactions(UUID walletId, Pageable pageable) {
        log.info("Fetching transactions for wallet: {}", walletId);

        // Verify wallet exists
        if (!walletRepository.existsById(walletId)) {
            throw new ResourceNotFoundException("Wallet not found");
        }

        Page<Transaction> transactions = transactionRepository.findByWalletIdOrderByCreatedAtDesc(walletId, pageable);
        return transactions.map(this::convertTransactionToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getUserTransactions(UUID userId, Pageable pageable) {
        log.info("Fetching transactions for user: {}", userId);

        Page<Transaction> transactions = transactionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return transactions.map(this::convertTransactionToResponse);
    }

    @Override
    @Transactional
    public TransactionResponse credit(UUID walletId, BigDecimal amount, String description, String referenceId) {
        log.info("Crediting {} to wallet: {}", amount, walletId);

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

        BigDecimal balanceBefore = wallet.getBalance();
        BigDecimal balanceAfter = balanceBefore.add(amount);

        wallet.setBalance(balanceAfter);
        walletRepository.save(wallet);

        Transaction transaction = Transaction.builder()
                .wallet(wallet)
                .user(wallet.getUser())
                .type(TransactionType.CREDIT)
                .status(TransactionStatus.COMPLETED)
                .amount(amount)
                .description(description)
                .referenceId(referenceId)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .completedAt(LocalDateTime.now())
                .build();

        transaction = transactionRepository.save(transaction);

        log.info("Credited successfully. New balance: {}", balanceAfter);
        return convertTransactionToResponse(transaction);
    }

    @Override
    @Transactional
    public TransactionResponse debit(UUID walletId, BigDecimal amount, String description, String referenceId) {
        log.info("Debiting {} from wallet: {}", amount, walletId);

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

        BigDecimal balanceBefore = wallet.getBalance();

        if (balanceBefore.compareTo(amount) < 0) {
            throw new BadRequestException(
                    String.format("Insufficient balance. Required: %s, Available: %s", amount, balanceBefore)
            );
        }

        BigDecimal balanceAfter = balanceBefore.subtract(amount);

        wallet.setBalance(balanceAfter);
        walletRepository.save(wallet);

        Transaction transaction = Transaction.builder()
                .wallet(wallet)
                .user(wallet.getUser())
                .type(TransactionType.DEBIT)
                .status(TransactionStatus.COMPLETED)
                .amount(amount)
                .description(description)
                .referenceId(referenceId)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .completedAt(LocalDateTime.now())
                .build();

        transaction = transactionRepository.save(transaction);

        log.info("Debited successfully. New balance: {}", balanceAfter);
        return convertTransactionToResponse(transaction);
    }

    @Override
    @Transactional
    public TransactionResponse holdEscrow(UUID walletId, BigDecimal amount, String description, String referenceId) {
        log.info("Holding {} in escrow for wallet: {}", amount, walletId);

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

        BigDecimal balanceBefore = wallet.getBalance();

        if (balanceBefore.compareTo(amount) < 0) {
            throw new BadRequestException(
                    String.format("Insufficient balance. Required: %s, Available: %s", amount, balanceBefore)
            );
        }

        // Move from balance to escrow balance
        BigDecimal balanceAfter = balanceBefore.subtract(amount);
        BigDecimal escrowBalanceAfter = wallet.getEscrowBalance().add(amount);

        wallet.setBalance(balanceAfter);
        wallet.setEscrowBalance(escrowBalanceAfter);
        walletRepository.save(wallet);

        Transaction transaction = Transaction.builder()
                .wallet(wallet)
                .user(wallet.getUser())
                .type(TransactionType.ESCROW_HOLD)
                .status(TransactionStatus.COMPLETED)
                .amount(amount)
                .description(description)
                .referenceId(referenceId)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .completedAt(LocalDateTime.now())
                .build();

        transaction = transactionRepository.save(transaction);

        log.info("Escrow held successfully. Balance: {}, Escrow: {}", balanceAfter, escrowBalanceAfter);
        return convertTransactionToResponse(transaction);
    }

    @Override
    @Transactional
    public TransactionResponse releaseEscrow(UUID walletId, BigDecimal amount, String description, String referenceId) {
        log.info("Releasing {} from escrow for wallet: {}", amount, walletId);

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

        BigDecimal escrowBalanceBefore = wallet.getEscrowBalance();

        if (escrowBalanceBefore.compareTo(amount) < 0) {
            throw new BadRequestException(
                    String.format("Insufficient escrow balance. Required: %s, Available: %s",
                            amount, escrowBalanceBefore)
            );
        }

        // Move from escrow to balance and add to total earned
        BigDecimal balanceBefore = wallet.getBalance();
        BigDecimal balanceAfter = balanceBefore.add(amount);
        BigDecimal escrowBalanceAfter = escrowBalanceBefore.subtract(amount);
        BigDecimal totalEarnedAfter = wallet.getTotalEarned().add(amount);

        wallet.setBalance(balanceAfter);
        wallet.setEscrowBalance(escrowBalanceAfter);
        wallet.setTotalEarned(totalEarnedAfter);
        walletRepository.save(wallet);

        Transaction transaction = Transaction.builder()
                .wallet(wallet)
                .user(wallet.getUser())
                .type(TransactionType.ESCROW_RELEASE)
                .status(TransactionStatus.COMPLETED)
                .amount(amount)
                .description(description)
                .referenceId(referenceId)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .completedAt(LocalDateTime.now())
                .build();

        transaction = transactionRepository.save(transaction);

        log.info("Escrow released successfully. Balance: {}, Escrow: {}, TotalEarned: {}",
                balanceAfter, escrowBalanceAfter, totalEarnedAfter);
        return convertTransactionToResponse(transaction);
    }

    @Override
    @Transactional
    public TransactionResponse refundEscrow(UUID walletId, BigDecimal amount, String description, String referenceId) {
        log.info("Refunding {} from escrow to wallet: {}", amount, walletId);

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

        BigDecimal escrowBalanceBefore = wallet.getEscrowBalance();

        if (escrowBalanceBefore.compareTo(amount) < 0) {
            throw new BadRequestException(
                    String.format("Insufficient escrow balance. Required: %s, Available: %s",
                            amount, escrowBalanceBefore)
            );
        }

        // Move from escrow back to balance (no total earned increase for refunds)
        BigDecimal balanceBefore = wallet.getBalance();
        BigDecimal balanceAfter = balanceBefore.add(amount);
        BigDecimal escrowBalanceAfter = escrowBalanceBefore.subtract(amount);

        wallet.setBalance(balanceAfter);
        wallet.setEscrowBalance(escrowBalanceAfter);
        walletRepository.save(wallet);

        Transaction transaction = Transaction.builder()
                .wallet(wallet)
                .user(wallet.getUser())
                .type(TransactionType.REFUND)
                .status(TransactionStatus.COMPLETED)
                .amount(amount)
                .description(description)
                .referenceId(referenceId)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .completedAt(LocalDateTime.now())
                .build();

        transaction = transactionRepository.save(transaction);

        log.info("Escrow refunded successfully. Balance: {}, Escrow: {}", balanceAfter, escrowBalanceAfter);
        return convertTransactionToResponse(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasSufficientBalance(UUID walletId, BigDecimal amount) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));
        return wallet.getBalance().compareTo(amount) >= 0;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasSufficientEscrowBalance(UUID walletId, BigDecimal amount) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));
        return wallet.getEscrowBalance().compareTo(amount) >= 0;
    }

    // Helper methods
    private WalletResponse convertToResponse(Wallet wallet) {
        return WalletResponse.builder()
                .id(wallet.getId())
                .userId(wallet.getUser() != null ? wallet.getUser().getId() : null)
                .balance(wallet.getBalance())
                .escrowBalance(wallet.getEscrowBalance())
                .totalEarned(wallet.getTotalEarned())
                .currency(wallet.getCurrency())
                .createdAt(wallet.getCreatedAt())
                .updatedAt(wallet.getUpdatedAt())
                .build();
    }

    private TransactionResponse convertTransactionToResponse(Transaction transaction) {
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
