package com.FreelancerUp;

import com.FreelancerUp.model.document.*;
import com.FreelancerUp.model.enums.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("dev")
class DocumentTest {

    @Test
    void testProjectDocumentCreation() {
        Project.ProjectBudget budget = Project.ProjectBudget.builder()
                .minAmount(new BigDecimal("500"))
                .maxAmount(new BigDecimal("1000"))
                .currency("USD")
                .isNegotiable(true)
                .build();

        Project project = Project.builder()
                .id(UUID.randomUUID().toString())
                .clientId(UUID.randomUUID().toString())
                .title("Build a REST API")
                .description("Need a Spring Boot REST API for a freelancer marketplace")
                .requirements("Java 17, Spring Boot 4.0, PostgreSQL, MongoDB")
                .skills(List.of("Java", "Spring Boot", "PostgreSQL", "MongoDB"))
                .budget(budget)
                .duration(30)
                .status(ProjectStatus.OPEN)
                .type(ProjectType.FIXED_PRICE)
                .deadline(LocalDateTime.now().plusDays(30))
                .build();

        assertThat(project).isNotNull();
        assertThat(project.getTitle()).isEqualTo("Build a REST API");
        assertThat(project.getStatus()).isEqualTo(ProjectStatus.OPEN);
        assertThat(project.getType()).isEqualTo(ProjectType.FIXED_PRICE);
        assertThat(project.getBudget().getMinAmount()).isEqualByComparingTo("500");
        assertThat(project.getBudget().getIsNegotiable()).isTrue();
        assertThat(project.getSkills()).hasSize(4);
    }

    @Test
    void testProjectBudgetDefaultValues() {
        Project.ProjectBudget budget = new Project.ProjectBudget();

        assertThat(budget.getCurrency()).isEqualTo("USD");
        assertThat(budget.getIsNegotiable()).isFalse();
    }

    @Test
    void testBidDocumentCreation() {
        Bid bid = Bid.builder()
                .id(UUID.randomUUID().toString())
                .projectId(UUID.randomUUID().toString())
                .freelancerId(UUID.randomUUID().toString())
                .proposal("I have 5 years of experience with Spring Boot and can deliver this project in 2 weeks.")
                .price(new BigDecimal("800"))
                .estimatedDuration(14)
                .status(BidStatus.PENDING)
                .submittedAt(LocalDateTime.now())
                .build();

        assertThat(bid).isNotNull();
        assertThat(bid.getProposal()).isNotEmpty();
        assertThat(bid.getPrice()).isEqualByComparingTo("800");
        assertThat(bid.getEstimatedDuration()).isEqualTo(14);
        assertThat(bid.getStatus()).isEqualTo(BidStatus.PENDING);
    }

    @Test
    void testFreelancerDocumentCreation() {
        Freelancer.FreelancerSkill skill1 = Freelancer.FreelancerSkill.builder()
                .skillId("skill-1")
                .name("Java")
                .proficiencyLevel(ProficiencyLevel.EXPERT)
                .yearsOfExperience(5)
                .build();

        Freelancer.FreelancerSkill skill2 = Freelancer.FreelancerSkill.builder()
                .skillId("skill-2")
                .name("Spring Boot")
                .proficiencyLevel(ProficiencyLevel.ADVANCED)
                .yearsOfExperience(3)
                .build();

        Freelancer freelancer = Freelancer.builder()
                .id(UUID.randomUUID().toString())
                .userId(UUID.randomUUID().toString())
                .bio("Full-stack Java developer with 5 years of experience")
                .hourlyRate(new BigDecimal("50"))
                .availability(Availability.AVAILABLE)
                .totalEarned(new BigDecimal("25000"))
                .completedProjects(15)
                .successRate(98.5)
                .skills(List.of(skill1, skill2))
                .build();

        assertThat(freelancer).isNotNull();
        assertThat(freelancer.getBio()).isNotEmpty();
        assertThat(freelancer.getHourlyRate()).isEqualByComparingTo("50");
        assertThat(freelancer.getAvailability()).isEqualTo(Availability.AVAILABLE);
        assertThat(freelancer.getCompletedProjects()).isEqualTo(15);
        assertThat(freelancer.getSuccessRate()).isEqualTo(98.5);
        assertThat(freelancer.getSkills()).hasSize(2);
        assertThat(freelancer.getSkills().get(0).getProficiencyLevel()).isEqualTo(ProficiencyLevel.EXPERT);
    }

    @Test
    void testFreelancerDefaultValues() {
        Freelancer freelancer = Freelancer.builder()
                .id(UUID.randomUUID().toString())
                .userId(UUID.randomUUID().toString())
                .build();

        assertThat(freelancer.getAvailability()).isEqualTo(Availability.AVAILABLE);
        assertThat(freelancer.getTotalEarned()).isEqualByComparingTo("0");
        assertThat(freelancer.getCompletedProjects()).isEqualTo(0);
        assertThat(freelancer.getSuccessRate()).isEqualTo(0.0);
        assertThat(freelancer.getSkills()).isNotNull();
    }

    @Test
    void testFreelancerSkillCreation() {
        Freelancer.FreelancerSkill skill = Freelancer.FreelancerSkill.builder()
                .skillId("skill-123")
                .name("React")
                .proficiencyLevel(ProficiencyLevel.INTERMEDIATE)
                .yearsOfExperience(2)
                .build();

        assertThat(skill).isNotNull();
        assertThat(skill.getName()).isEqualTo("React");
        assertThat(skill.getProficiencyLevel()).isEqualTo(ProficiencyLevel.INTERMEDIATE);
        assertThat(skill.getYearsOfExperience()).isEqualTo(2);
    }

    @Test
    void testMessageDocumentCreation() {
        Message message = Message.builder()
                .id(UUID.randomUUID().toString())
                .conversationId(UUID.randomUUID().toString())
                .projectId(UUID.randomUUID().toString())
                .fromUserId(UUID.randomUUID().toString())
                .toUserId(UUID.randomUUID().toString())
                .content("Hello, I'm interested in your project. Can we discuss the requirements?")
                .isRead(false)
                .build();

        assertThat(message).isNotNull();
        assertThat(message.getContent()).isNotEmpty();
        assertThat(message.getIsRead()).isFalse();
        assertThat(message.getReadAt()).isNull();
    }

    @Test
    void testMessageWithReadStatus() {
        Message message = Message.builder()
                .id(UUID.randomUUID().toString())
                .conversationId(UUID.randomUUID().toString())
                .fromUserId(UUID.randomUUID().toString())
                .toUserId(UUID.randomUUID().toString())
                .content("Thank you for the response!")
                .isRead(true)
                .readAt(LocalDateTime.now())
                .build();

        assertThat(message).isNotNull();
        assertThat(message.getIsRead()).isTrue();
        assertThat(message.getReadAt()).isNotNull();
    }

    @Test
    void testMessagePrePersistHook() {
        Message message = new Message();
        message.setId(UUID.randomUUID().toString());
        message.setConversationId(UUID.randomUUID().toString());
        message.setFromUserId(UUID.randomUUID().toString());
        message.setToUserId(UUID.randomUUID().toString());
        message.setContent("Test message");
        message.setIsRead(true);

        // Simulate @PrePersist
        message.prePersist();

        assertThat(message.getReadAt()).isNotNull();
    }

    @Test
    void testExperienceDocumentCreation() {
        Experience experience = Experience.builder()
                .id(UUID.randomUUID().toString())
                .freelancerId(UUID.randomUUID().toString())
                .title("Senior Java Developer")
                .company("Tech Corp")
                .location("Remote")
                .startDate(LocalDateTime.of(2020, 1, 1, 0, 0))
                .endDate(LocalDateTime.of(2023, 12, 31, 0, 0))
                .isCurrentJob(false)
                .description("Developed and maintained microservices architecture")
                .skills(List.of("Java", "Spring Boot", "Kubernetes"))
                .build();

        assertThat(experience).isNotNull();
        assertThat(experience.getTitle()).isEqualTo("Senior Java Developer");
        assertThat(experience.getCompany()).isEqualTo("Tech Corp");
        assertThat(experience.getIsCurrentJob()).isFalse();
        assertThat(experience.getSkills()).hasSize(3);
    }

    @Test
    void testExperienceCurrentJob() {
        Experience experience = Experience.builder()
                .id(UUID.randomUUID().toString())
                .freelancerId(UUID.randomUUID().toString())
                .title("Full Stack Developer")
                .company("Startup Inc")
                .startDate(LocalDateTime.of(2023, 6, 1, 0, 0))
                .endDate(null) // Current job has no end date
                .isCurrentJob(true)
                .build();

        assertThat(experience).isNotNull();
        assertThat(experience.getIsCurrentJob()).isTrue();
        assertThat(experience.getEndDate()).isNull();
    }

    @Test
    void testEducationDocumentCreation() {
        Education education = Education.builder()
                .id(UUID.randomUUID().toString())
                .freelancerId(UUID.randomUUID().toString())
                .school("Massachusetts Institute of Technology")
                .degree("Bachelor of Science")
                .fieldOfStudy("Computer Science")
                .startDate(LocalDateTime.of(2016, 9, 1, 0, 0))
                .endDate(LocalDateTime.of(2020, 5, 31, 0, 0))
                .description("Graduated with honors, GPA 3.8")
                .build();

        assertThat(education).isNotNull();
        assertThat(education.getSchool()).isEqualTo("Massachusetts Institute of Technology");
        assertThat(education.getDegree()).isEqualTo("Bachelor of Science");
        assertThat(education.getFieldOfStudy()).isEqualTo("Computer Science");
    }

    @Test
    void testProjectWithFreelancerAssigned() {
        Project.ProjectBudget budget = Project.ProjectBudget.builder()
                .minAmount(new BigDecimal("1000"))
                .maxAmount(new BigDecimal("1500"))
                .build();

        Project project = Project.builder()
                .id(UUID.randomUUID().toString())
                .clientId(UUID.randomUUID().toString())
                .freelancerId(UUID.randomUUID().toString())
                .title("Mobile App Development")
                .status(ProjectStatus.IN_PROGRESS)
                .type(ProjectType.HOURLY)
                .budget(budget)
                .startedAt(LocalDateTime.now())
                .contractId(UUID.randomUUID().toString())
                .build();

        assertThat(project).isNotNull();
        assertThat(project.getFreelancerId()).isNotNull();
        assertThat(project.getStatus()).isEqualTo(ProjectStatus.IN_PROGRESS);
        assertThat(project.getStartedAt()).isNotNull();
        assertThat(project.getContractId()).isNotNull();
    }

    @Test
    void testBidAcceptedStatus() {
        Bid bid = Bid.builder()
                .id(UUID.randomUUID().toString())
                .projectId(UUID.randomUUID().toString())
                .freelancerId(UUID.randomUUID().toString())
                .proposal("I can complete this project")
                .price(new BigDecimal("1200"))
                .status(BidStatus.ACCEPTED)
                .respondedAt(LocalDateTime.now())
                .build();

        assertThat(bid).isNotNull();
        assertThat(bid.getStatus()).isEqualTo(BidStatus.ACCEPTED);
        assertThat(bid.getRespondedAt()).isNotNull();
    }

    @Test
    void testProjectCompleted() {
        Project project = Project.builder()
                .id(UUID.randomUUID().toString())
                .clientId(UUID.randomUUID().toString())
                .freelancerId(UUID.randomUUID().toString())
                .title("Completed Project")
                .status(ProjectStatus.COMPLETED)
                .completedAt(LocalDateTime.now())
                .build();

        assertThat(project).isNotNull();
        assertThat(project.getStatus()).isEqualTo(ProjectStatus.COMPLETED);
        assertThat(project.getCompletedAt()).isNotNull();
    }
}
