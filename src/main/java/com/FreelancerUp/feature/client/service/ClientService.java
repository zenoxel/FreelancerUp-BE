package com.FreelancerUp.feature.client.service;

import com.FreelancerUp.feature.client.dto.request.RegisterClientRequest;
import com.FreelancerUp.feature.client.dto.request.UpdateClientProfileRequest;
import com.FreelancerUp.feature.client.dto.response.ClientProfileResponse;
import com.FreelancerUp.feature.client.dto.response.ClientStatsResponse;

import java.util.UUID;

public interface ClientService {

    ClientProfileResponse registerClient(UUID userId, RegisterClientRequest request);

    ClientProfileResponse getClientProfile(UUID userId);

    ClientProfileResponse updateClientProfile(UUID userId, UpdateClientProfileRequest request);

    ClientStatsResponse getClientStats(UUID userId);

    void deleteClient(UUID userId);
}
