package com.FreelancerUp.feature.project.repository;

import com.FreelancerUp.model.document.Project;
import com.FreelancerUp.model.enums.ProjectStatus;
import com.FreelancerUp.model.enums.ProjectType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProjectRepository extends MongoRepository<Project, String> {

    List<Project> findByClientId(String clientId);

    List<Project> findByFreelancerId(String freelancerId);

    List<Project> findByStatus(ProjectStatus status);

    List<Project> findByType(ProjectType type);

    // Search by skills (at least one match)
    List<Project> findBySkillsIn(List<String> skills);

    // Search by budget range
    @Query("{'budget.minAmount': {$gte: ?0}, 'budget.maxAmount': {$lte: ?1}}")
    List<Project> findByBudgetRange(BigDecimal minAmount, BigDecimal maxAmount);

    // Complex search with filters
    @Query("{$and: [" +
           "{$or: [{'status': ?0}, {'status': ?1}]}, " +
           "{'budget.maxAmount': {$lte: ?2}}, " +
           "{'skills': {$in: ?3}}" +
           "]}")
    List<Project> searchProjects(
        ProjectStatus status1,
        ProjectStatus status2,
        BigDecimal maxBudget,
        List<String> skills
    );

    // Full-text search on title/description
    @Query("{$or: [" +
           "{'title': {$regex: ?0, $options: 'i'}}, " +
           "{'description': {$regex: ?0, $options: 'i'}}" +
           "]}")
    List<Project> searchByKeyword(String keyword);

    // Find expiring projects
    List<Project> findByDeadlineBeforeAndStatus(
        LocalDateTime deadline,
        ProjectStatus status
    );
}
