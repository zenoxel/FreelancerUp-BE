package com.FreelancerUp.feature.client.service.impl;

import com.FreelancerUp.exception.ConflictException;
import com.FreelancerUp.exception.ResourceNotFoundException;
import com.FreelancerUp.feature.client.dto.request.RegisterClientRequest;
import com.FreelancerUp.feature.client.dto.request.UpdateClientProfileRequest;
import com.FreelancerUp.feature.client.dto.response.ClientProfileResponse;
import com.FreelancerUp.feature.client.dto.response.ClientStatsResponse;
import com.FreelancerUp.feature.client.service.ClientService;
import com.FreelancerUp.feature.common.dto.PaymentMethodDTO;
import com.FreelancerUp.model.entity.Client;
import com.FreelancerUp.model.entity.User;
import com.FreelancerUp.model.enums.PaymentMethod;
import com.FreelancerUp.model.enums.Role;
import com.FreelancerUp.feature.client.repository.ClientRepository;
import com.FreelancerUp.feature.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final UserRepository userRepository;

    @Override
    public ClientProfileResponse registerClient(UUID userId, RegisterClientRequest request) {
        log.info("Registering client profile for user: {}", userId);

        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if client profile already exists
        if (clientRepository.findById(userId).isPresent()) {
            throw new ConflictException("Client profile already exists");
        }

        // Create client
        Client client = Client.builder()
                .id(userId)
                .companyName(request.getCompanyName())
                .industry(request.getIndustry())
                .companySize(request.getCompanySize())
                .paymentMethods(convertPaymentMethods(request.getPaymentMethods()))
                .totalSpent(BigDecimal.ZERO)
                .postedProjects(0)
                .build();

        client = clientRepository.save(client);

        // Update user role
        user.setRole(Role.CLIENT);
        userRepository.save(user);

        log.info("Client profile registered successfully for user: {}", userId);
        return convertToResponse(client, user);
    }

    @Override
    @Transactional(readOnly = true)
    public ClientProfileResponse getClientProfile(UUID userId) {
        log.info("Fetching client profile for user: {}", userId);

        Client client = clientRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Client profile not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return convertToResponse(client, user);
    }

    @Override
    public ClientProfileResponse updateClientProfile(UUID userId, UpdateClientProfileRequest request) {
        log.info("Updating client profile for user: {}", userId);

        Client client = clientRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Client profile not found"));

        // Update fields if provided
        if (request.getCompanyName() != null) {
            client.setCompanyName(request.getCompanyName());
        }
        if (request.getIndustry() != null) {
            client.setIndustry(request.getIndustry());
        }
        if (request.getCompanySize() != null) {
            client.setCompanySize(request.getCompanySize());
        }
        if (request.getPaymentMethods() != null) {
            client.setPaymentMethods(convertPaymentMethods(request.getPaymentMethods()));
        }

        client = clientRepository.save(client);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        log.info("Client profile updated successfully for user: {}", userId);
        return convertToResponse(client, user);
    }

    @Override
    @Transactional(readOnly = true)
    public ClientStatsResponse getClientStats(UUID userId) {
        log.info("Fetching client stats for user: {}", userId);

        Client client = clientRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Client profile not found"));

        // Get project statistics
        // TODO: Implement after Project module is ready

        return ClientStatsResponse.builder()
                .clientId(client.getId())
                .companyName(client.getCompanyName())
                .totalProjects(client.getPostedProjects())
                .totalSpent(client.getTotalSpent())
                .build();
    }

    @Override
    public void deleteClient(UUID userId) {
        log.info("Deleting client profile for user: {}", userId);

        // Soft delete - mark user as inactive
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setIsActive(false);
        userRepository.save(user);

        // Delete client profile
        clientRepository.deleteById(userId);

        log.info("Client profile deleted successfully for user: {}", userId);
    }

    // Helper methods
    private ClientProfileResponse convertToResponse(Client client, User user) {
        return ClientProfileResponse.builder()
                .id(client.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .companyName(client.getCompanyName())
                .industry(client.getIndustry())
                .companySize(client.getCompanySize())
                .paymentMethods(convertToDTO(client.getPaymentMethods()))
                .totalSpent(client.getTotalSpent())
                .postedProjects(client.getPostedProjects())
                .createdAt(client.getCreatedAt())
                .updatedAt(client.getUpdatedAt())
                .build();
    }

    private List<PaymentMethod> convertPaymentMethods(List<PaymentMethodDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return new ArrayList<>();
        }

        List<PaymentMethod> paymentMethods = new ArrayList<>();
        for (PaymentMethodDTO dto : dtos) {
            paymentMethods.add(dto.getType());
        }
        return paymentMethods;
    }

    private List<PaymentMethodDTO> convertToDTO(List<PaymentMethod> entities) {
        if (entities == null || entities.isEmpty()) {
            return new ArrayList<>();
        }

        List<PaymentMethodDTO> dtos = new ArrayList<>();
        for (PaymentMethod entity : entities) {
            dtos.add(PaymentMethodDTO.builder()
                    .type(entity)
                    .build());
        }
        return dtos;
    }
}
