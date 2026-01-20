package com.FreelancerUp.feature.freelancer.repository;

import com.FreelancerUp.model.document.Experience;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExperienceRepository extends MongoRepository<Experience, String> {

    List<Experience> findByFreelancerIdOrderByStartDateDesc(String freelancerId);

    @Query("{'freelancerId': ?0, 'isCurrentJob': true}")
    List<Experience> findCurrentJobs(String freelancerId);

    Long countByFreelancerId(String freelancerId);
}
