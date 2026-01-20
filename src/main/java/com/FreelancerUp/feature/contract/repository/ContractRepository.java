package com.FreelancerUp.feature.contract.repository;

import com.FreelancerUp.model.entity.Contract;
import com.FreelancerUp.model.enums.ContractStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContractRepository extends JpaRepository<Contract, UUID> {

    Optional<Contract> findByProjectId(String projectId);

    List<Contract> findByClientId(UUID clientId);

    List<Contract> findByFreelancerId(UUID freelancerId);

    List<Contract> findByStatus(ContractStatus status);

    @Query("SELECT c FROM Contract c WHERE c.client.id = :userId " +
           "OR c.freelancer.id = :userId")
    List<Contract> findAllByUser(@Param("userId") UUID userId);
}
