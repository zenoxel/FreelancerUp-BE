package com.FreelancerUp.feature.payment.repository;

import com.FreelancerUp.model.entity.Transaction;
import com.FreelancerUp.model.enums.TransactionType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findByWalletIdOrderByCreatedAtDesc(UUID walletId, Pageable pageable);

    List<Transaction> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    List<Transaction> findByUserIdAndTypeAndCreatedAtBetween(
        UUID userId,
        TransactionType type,
        LocalDateTime startDate,
        LocalDateTime endDate
    );

    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId " +
           "AND t.type IN :types AND t.createdAt BETWEEN :start AND :end")
    List<Transaction> findByUserAndMultipleTypes(
        @Param("userId") UUID userId,
        @Param("types") List<TransactionType> types,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );
}
