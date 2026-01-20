package com.FreelancerUp.feature.payment.repository;

import com.FreelancerUp.model.entity.Payment;
import com.FreelancerUp.model.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findByProjectId(String projectId);

    List<Payment> findByFromUserId(UUID fromUserId);

    List<Payment> findByToUserId(UUID toUserId);

    List<Payment> findByStatus(PaymentStatus status);

    @Query("SELECT p FROM Payment p WHERE p.projectId = :projectId " +
           "AND p.status IN :statuses")
    List<Payment> findByProjectIdAndStatuses(
        @Param("projectId") String projectId,
        @Param("statuses") List<PaymentStatus> statuses
    );

    @Query("SELECT p FROM Payment p WHERE (p.fromUser.id = :userId OR p.toUser.id = :userId) " +
           "AND p.status = :status")
    List<Payment> findByFromUserIdOrToUserIdAndStatus(
        @Param("userId") UUID userId,
        @Param("status") PaymentStatus status
    );
}
