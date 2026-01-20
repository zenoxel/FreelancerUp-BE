package com.FreelancerUp.feature.freelancer.repository;

import com.FreelancerUp.model.document.Freelancer;
import com.FreelancerUp.model.enums.Availability;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface FreelancerRepository extends MongoRepository<Freelancer, String> {

    Optional<Freelancer> findByUserId(String userId);

    @Query("{'skills.name': {$in: ?0}}")
    List<Freelancer> findBySkills(List<String> skillNames);

    @Query("{$and: [" +
           "{'availability': ?0}, " +
           "{'hourlyRate': {$lte: ?1}}" +
           "]}")
    List<Freelancer> findAvailableFreelancersWithinBudget(
        Availability availability,
        BigDecimal maxHourlyRate
    );

    @Query("{$or: [" +
           "{'bio': {$regex: ?0, $options: 'i'}}, " +
           "{'skills.name': {$regex: ?0, $options: 'i'}}" +
           "]}")
    List<Freelancer> searchByKeyword(String keyword);

    @Query("{'successRate': {$gte: ?0}}")
    List<Freelancer> findByMinSuccessRate(Double minSuccessRate);

    @Query("{'completedProjects': {$gte: ?0}}")
    List<Freelancer> findByMinCompletedProjects(Integer minProjects);
}
