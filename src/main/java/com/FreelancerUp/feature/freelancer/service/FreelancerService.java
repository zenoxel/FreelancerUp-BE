package com.FreelancerUp.feature.freelancer.service;

import com.FreelancerUp.feature.freelancer.dto.request.RegisterFreelancerRequest;
import com.FreelancerUp.feature.freelancer.dto.request.UpdateFreelancerProfileRequest;
import com.FreelancerUp.feature.freelancer.dto.response.FreelancerProfileResponse;
import com.FreelancerUp.feature.freelancer.dto.response.FreelancerStatsResponse;

public interface FreelancerService {

    FreelancerProfileResponse registerFreelancer(String email, RegisterFreelancerRequest request);

    FreelancerProfileResponse getFreelancerProfile(String email);

    FreelancerProfileResponse updateFreelancerProfile(String email, UpdateFreelancerProfileRequest request);

    FreelancerStatsResponse getFreelancerStats(String email);

    void deleteFreelancer(String email);
}
