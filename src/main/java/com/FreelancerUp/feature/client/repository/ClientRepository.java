package com.FreelancerUp.feature.client.repository;

import com.FreelancerUp.model.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClientRepository extends JpaRepository<Client, UUID> {

    Optional<Client> findById(UUID userId);

    @Query("SELECT c FROM Client c WHERE c.companyName LIKE %:keyword%")
    List<Client> searchByCompanyName(@Param("keyword") String keyword);

    @Query("SELECT c FROM Client c ORDER BY c.totalSpent DESC")
    List<Client> findTopSpenders(Pageable pageable);
}
