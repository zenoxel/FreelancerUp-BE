package com.FreelancerUp.feature.freelancer.service.impl;

import com.FreelancerUp.exception.ConflictException;
import com.FreelancerUp.exception.ResourceNotFoundException;
import com.FreelancerUp.feature.freelancer.dto.request.RegisterFreelancerRequest;
import com.FreelancerUp.feature.freelancer.dto.request.UpdateFreelancerProfileRequest;
import com.FreelancerUp.feature.freelancer.dto.response.FreelancerProfileResponse;
import com.FreelancerUp.feature.freelancer.dto.response.FreelancerStatsResponse;
import com.FreelancerUp.feature.freelancer.service.FreelancerService;
import com.FreelancerUp.feature.freelancer.repository.FreelancerRepository;
import com.FreelancerUp.feature.user.repository.UserRepository;
import com.FreelancerUp.model.document.Freelancer;
import com.FreelancerUp.model.document.Freelancer.FreelancerSkill;
import com.FreelancerUp.model.entity.User;
import com.FreelancerUp.model.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FreelancerServiceImpl implements FreelancerService {

    private final FreelancerRepository freelancerRepository;
    private final UserRepository userRepository;

    @Override
    public FreelancerProfileResponse registerFreelancer(String email, RegisterFreelancerRequest request) {
        log.info("Registering freelancer profile for email: {}", email);

        // Validate user exists
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UUID userId = user.getId();

        // Check if freelancer profile already exists
        if (freelancerRepository.findByUserId(userId.toString()).isPresent()) {
            throw new ConflictException("Freelancer profile already exists");
        }

        // Convert skills
        List<FreelancerSkill> skills = convertRegisterSkillsToEntities(request.getSkills());

        // Create freelancer
        Freelancer freelancer = Freelancer.builder()
                .userId(userId.toString())
                .bio(request.getBio())
                .hourlyRate(request.getHourlyRate())
                .availability(request.getAvailability())
                .totalEarned(BigDecimal.ZERO)
                .completedProjects(0)
                .successRate(0.0)
                .skills(skills)
                .build();

        freelancer = freelancerRepository.save(freelancer);

        // Update user role
        user.setRole(Role.FREELANCER);
        userRepository.save(user);

        log.info("Freelancer profile registered successfully for email: {}", email);
        return convertToResponse(freelancer, user);
    }

    @Override
    @Transactional(readOnly = true)
    public FreelancerProfileResponse getFreelancerProfile(String email) {
        log.info("Fetching freelancer profile for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UUID userId = user.getId();

        Freelancer freelancer = freelancerRepository.findByUserId(userId.toString())
                .orElseThrow(() -> new ResourceNotFoundException("Freelancer profile not found"));

        return convertToResponse(freelancer, user);
    }

    @Override
    public FreelancerProfileResponse updateFreelancerProfile(String email, UpdateFreelancerProfileRequest request) {
        log.info("Updating freelancer profile for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UUID userId = user.getId();

        Freelancer freelancer = freelancerRepository.findByUserId(userId.toString())
                .orElseThrow(() -> new ResourceNotFoundException("Freelancer profile not found"));

        // Update fields if provided
        if (request.getBio() != null) {
            freelancer.setBio(request.getBio());
        }
        if (request.getHourlyRate() != null) {
            freelancer.setHourlyRate(request.getHourlyRate());
        }
        if (request.getAvailability() != null) {
            freelancer.setAvailability(request.getAvailability());
        }
        if (request.getSkills() != null) {
            List<FreelancerSkill> skills = convertUpdateSkillsToEntities(request.getSkills());
            freelancer.setSkills(skills);
        }

        freelancer = freelancerRepository.save(freelancer);

        log.info("Freelancer profile updated successfully for email: {}", email);
        return convertToResponse(freelancer, user);
    }

    @Override
    @Transactional(readOnly = true)
    public FreelancerStatsResponse getFreelancerStats(String email) {
        log.info("Fetching freelancer stats for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UUID userId = user.getId();

        Freelancer freelancer = freelancerRepository.findByUserId(userId.toString())
                .orElseThrow(() -> new ResourceNotFoundException("Freelancer profile not found"));

        // Get project statistics
        // TODO: Implement after Project module is ready
        // TODO: Get wallet balance from Wallet module

        return FreelancerStatsResponse.builder()
                .freelancerId(userId)
                .fullName(user.getFullName())
                .totalProjects(freelancer.getCompletedProjects())
                .activeProjects(0)
                .completedProjects(freelancer.getCompletedProjects())
                .totalEarned(freelancer.getTotalEarned())
                .availableBalance(BigDecimal.ZERO)
                .escrowBalance(BigDecimal.ZERO)
                .averageRating(0.0)
                .totalReviews(0)
                .successRate(freelancer.getSuccessRate())
                .build();
    }

    @Override
    public void deleteFreelancer(String email) {
        log.info("Deleting freelancer profile for email: {}", email);

        // Soft delete - mark user as inactive
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UUID userId = user.getId();

        user.setIsActive(false);
        userRepository.save(user);

        // Delete freelancer profile
        freelancerRepository.findByUserId(userId.toString()).ifPresent(freelancer ->
                freelancerRepository.delete(freelancer));

        log.info("Freelancer profile deleted successfully for email: {}", email);
    }

    // Helper methods
    private FreelancerProfileResponse convertToResponse(Freelancer freelancer, User user) {
        List<FreelancerProfileResponse.FreelancerSkillDTO> skillDTOs = freelancer.getSkills().stream()
                .map(this::convertToSkillDTO)
                .collect(Collectors.toList());

        return FreelancerProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .bio(freelancer.getBio())
                .hourlyRate(freelancer.getHourlyRate())
                .availability(freelancer.getAvailability())
                .totalEarned(freelancer.getTotalEarned())
                .completedProjects(freelancer.getCompletedProjects())
                .successRate(freelancer.getSuccessRate())
                .skills(skillDTOs)
                .createdAt(freelancer.getCreatedAt())
                .updatedAt(freelancer.getUpdatedAt())
                .build();
    }

    private FreelancerProfileResponse.FreelancerSkillDTO convertToSkillDTO(FreelancerSkill skill) {
        return FreelancerProfileResponse.FreelancerSkillDTO.builder()
                .skillId(skill.getSkillId())
                .name(skill.getName())
                .proficiencyLevel(skill.getProficiencyLevel())
                .yearsOfExperience(skill.getYearsOfExperience())
                .build();
    }

    private List<FreelancerSkill> convertRegisterSkillsToEntities(List<RegisterFreelancerRequest.FreelancerSkillDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return new ArrayList<>();
        }

        return dtos.stream()
                .map(dto -> FreelancerSkill.builder()
                        .skillId(dto.getSkillId() != null ? dto.getSkillId() : UUID.randomUUID().toString())
                        .name(dto.getName())
                        .proficiencyLevel(dto.getProficiencyLevel())
                        .yearsOfExperience(dto.getYearsOfExperience())
                        .build())
                .collect(Collectors.toList());
    }

    private List<FreelancerSkill> convertUpdateSkillsToEntities(List<UpdateFreelancerProfileRequest.FreelancerSkillDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return new ArrayList<>();
        }

        return dtos.stream()
                .map(dto -> FreelancerSkill.builder()
                        .skillId(dto.getSkillId() != null ? dto.getSkillId() : UUID.randomUUID().toString())
                        .name(dto.getName())
                        .proficiencyLevel(dto.getProficiencyLevel())
                        .yearsOfExperience(dto.getYearsOfExperience())
                        .build())
                .collect(Collectors.toList());
    }
}
