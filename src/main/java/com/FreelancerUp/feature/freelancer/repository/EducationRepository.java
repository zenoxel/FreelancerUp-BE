package com.FreelancerUp.feature.freelancer.repository;

import com.FreelancerUp.model.document.Education;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EducationRepository extends MongoRepository<Education, String> {

    List<Education> findByFreelancerIdOrderByEndDateDesc(String freelancerId);

    @Query("{'freelancerId': ?0, 'fieldOfStudy': {$regex: ?1, $options: 'i'}}")
    List<Education> findByFreelancerIdAndFieldOfStudy(String freelancerId, String fieldOfStudy);
}
