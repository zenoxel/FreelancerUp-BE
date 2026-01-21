package com.FreelancerUp.feature.bid.repository;

import com.FreelancerUp.model.document.Bid;
import com.FreelancerUp.model.enums.BidStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BidRepository extends MongoRepository<Bid, String> {

    List<Bid> findByProjectId(String projectId);

    List<Bid> findByFreelancerId(String freelancerId);

    Optional<Bid> findByProjectIdAndFreelancerId(String projectId, String freelancerId);

    Optional<Bid> findByProjectIdAndStatus(String projectId, BidStatus status);

    List<Bid> findByStatus(BidStatus status);

    @Query("{'projectId': ?0, $or: [{'status': 'PENDING'}, {'status': 'ACCEPTED'}]}")
    List<Bid> findActiveBidsForProject(String projectId);

    @Query("{'freelancerId': ?0, $or: [{'status': 'PENDING'}, {'status': 'ACCEPTED'}]}")
    List<Bid> findActiveBidsForFreelancer(String freelancerId);

    Long countByProjectIdAndStatus(String projectId, BidStatus status);
}
