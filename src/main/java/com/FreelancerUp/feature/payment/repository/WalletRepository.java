package com.FreelancerUp.feature.payment.repository;

import com.FreelancerUp.model.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    Optional<Wallet> findById(UUID id);

    @Query("SELECT w FROM Wallet w WHERE w.user.id = :userId")
    Optional<Wallet> findByUserId(@Param("userId") UUID userId);

    @Query("SELECT w FROM Wallet w WHERE w.balance > :minAmount")
    List<Wallet> findWalletsWithMinBalance(@Param("minAmount") BigDecimal minAmount);

    @Modifying
    @Query("UPDATE Wallet w SET w.balance = w.balance + :amount WHERE w.id = :id")
    void incrementBalance(@Param("id") UUID id, @Param("amount") BigDecimal amount);

    @Modifying
    @Query("UPDATE Wallet w SET w.escrowBalance = w.escrowBalance + :amount WHERE w.id = :id")
    void incrementEscrowBalance(@Param("id") UUID id, @Param("amount") BigDecimal amount);
}
