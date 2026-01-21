package com.FreelancerUp.feature.payment.controller;

import com.FreelancerUp.feature.payment.dto.response.TransactionResponse;
import com.FreelancerUp.feature.payment.dto.response.WalletResponse;
import com.FreelancerUp.feature.payment.service.TransactionService;
import com.FreelancerUp.feature.payment.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller for Wallet operations.
 * Provides endpoints for wallet balance and transaction history.
 */
@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
@Tag(name = "Wallet", description = "Wallet management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class WalletController {

    private final WalletService walletService;
    private final TransactionService transactionService;

    @GetMapping("/my")
    @Operation(summary = "Get current user's wallet", description = "Returns the wallet information for the authenticated user")
    public ResponseEntity<WalletResponse> getMyWallet(@AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        WalletResponse wallet = walletService.getWalletByUserId(userId);
        return ResponseEntity.ok(wallet);
    }

    @GetMapping("/{walletId}")
    @Operation(summary = "Get wallet by ID", description = "Returns wallet information by wallet ID")
    public ResponseEntity<WalletResponse> getWalletById(
            @Parameter(description = "Wallet ID") @PathVariable UUID walletId) {
        WalletResponse wallet = walletService.getWalletById(walletId);
        return ResponseEntity.ok(wallet);
    }

    @GetMapping("/{walletId}/transactions")
    @Operation(summary = "Get wallet transactions", description = "Returns transaction history for a specific wallet")
    public ResponseEntity<Page<TransactionResponse>> getWalletTransactions(
            @Parameter(description = "Wallet ID") @PathVariable UUID walletId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "DESC") String sortDirection) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), "createdAt");
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<TransactionResponse> transactions = walletService.getWalletTransactions(walletId, pageable);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/my/transactions")
    @Operation(summary = "Get current user's transactions", description = "Returns transaction history for the authenticated user")
    public ResponseEntity<Page<TransactionResponse>> getMyTransactions(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "DESC") String sortDirection) {

        UUID userId = UUID.fromString(userDetails.getUsername());
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), "createdAt");
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<TransactionResponse> transactions = transactionService.getUserTransactions(userId, pageable);
        return ResponseEntity.ok(transactions);
    }

    @PostMapping("/create")
    @Operation(summary = "Create wallet for user", description = "Creates a new wallet for the authenticated user if not exists")
    public ResponseEntity<WalletResponse> createWallet(@AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        WalletResponse wallet = walletService.createWalletForUser(userId);
        return ResponseEntity.ok(wallet);
    }
}
