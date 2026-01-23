package com.FreelancerUp.feature.client.service;

import com.FreelancerUp.feature.client.dto.request.RegisterClientRequest;
import com.FreelancerUp.feature.client.dto.request.UpdateClientProfileRequest;
import com.FreelancerUp.feature.client.dto.response.ClientProfileResponse;
import com.FreelancerUp.feature.client.dto.response.ClientStatsResponse;

public interface ClientService {

    ClientProfileResponse registerClient(String email, RegisterClientRequest request);

    ClientProfileResponse getClientProfile(String email);

    ClientProfileResponse updateClientProfile(String email, UpdateClientProfileRequest request);

    ClientStatsResponse getClientStats(String email);

    void deleteClient(String email);
}
