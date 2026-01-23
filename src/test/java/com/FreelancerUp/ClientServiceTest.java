package com.FreelancerUp;

import com.FreelancerUp.exception.ConflictException;
import com.FreelancerUp.exception.ResourceNotFoundException;
import com.FreelancerUp.feature.client.dto.request.RegisterClientRequest;
import com.FreelancerUp.feature.client.dto.request.UpdateClientProfileRequest;
import com.FreelancerUp.feature.client.dto.response.ClientProfileResponse;
import com.FreelancerUp.feature.client.dto.response.ClientStatsResponse;
import com.FreelancerUp.feature.client.service.ClientService;
import com.FreelancerUp.model.entity.Client;
import com.FreelancerUp.model.entity.User;
import com.FreelancerUp.model.enums.CompanySize;
import com.FreelancerUp.model.enums.PaymentMethod;
import com.FreelancerUp.model.enums.Role;
import com.FreelancerUp.feature.client.repository.ClientRepository;
import com.FreelancerUp.feature.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Client Service Tests")
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private UserRepository userRepository;

    // Use the actual implementation class
    private com.FreelancerUp.feature.client.service.impl.ClientServiceImpl clientServiceImpl;

    private String userEmail;
    private UUID userId;
    private User user;
    private Client client;

    @BeforeEach
    void setUp() {
        clientServiceImpl = new com.FreelancerUp.feature.client.service.impl.ClientServiceImpl(clientRepository, userRepository);

        userId = UUID.randomUUID();
        userEmail = "client@example.com";

        user = User.builder()
                .id(userId)
                .email(userEmail)
                .fullName("John Doe")
                .avatarUrl("http://example.com/avatar.jpg")
                .role(Role.USER)
                .isActive(true)
                .isEmailVerified(true)
                .build();

        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setType("CREDIT_CARD");
        paymentMethod.setProvider("Visa");
        paymentMethod.setLastFourDigits("4242");
        paymentMethod.setIsDefault(true);
        paymentMethod.setIsActive(true);

        client = Client.builder()
                .id(userId)
                .companyName("Tech Corp")
                .industry("Technology")
                .companySize(CompanySize.SIZE_11_50)
                .paymentMethods(List.of(paymentMethod))
                .totalSpent(BigDecimal.ZERO)
                .postedProjects(0)
                .build();
    }

    @Test
    @DisplayName("Should register client successfully")
    void testRegisterClient_Success() {
        // Given
        RegisterClientRequest request = RegisterClientRequest.builder()
                .companyName("Tech Corp")
                .industry("Technology")
                .companySize(CompanySize.SIZE_11_50)
                .paymentMethods(List.of())
                .build();

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(clientRepository.findById(userId)).thenReturn(Optional.empty());
        when(clientRepository.save(any(Client.class))).thenReturn(client);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        ClientProfileResponse response = clientServiceImpl.registerClient(userEmail, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(userId);
        assertThat(response.getCompanyName()).isEqualTo("Tech Corp");
        assertThat(response.getIndustry()).isEqualTo("Technology");
        assertThat(response.getCompanySize()).isEqualTo(CompanySize.SIZE_11_50);
        assertThat(response.getTotalSpent()).isEqualByComparingTo("0");
        assertThat(response.getPostedProjects()).isEqualTo(0);

        verify(userRepository).findByEmail(userEmail);
        verify(clientRepository).findById(userId);
        verify(clientRepository).save(any(Client.class));
        verify(userRepository).save(any(User.class));

        assertThat(user.getRole()).isEqualTo(Role.CLIENT);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user not found during registration")
    void testRegisterClient_UserNotFound() {
        // Given
        RegisterClientRequest request = RegisterClientRequest.builder()
                .companyName("Tech Corp")
                .build();

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> clientServiceImpl.registerClient(userEmail, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");

        verify(userRepository).findByEmail(userEmail);
        verify(clientRepository, never()).save(any(Client.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw ConflictException when client profile already exists")
    void testRegisterClient_ClientAlreadyExists() {
        // Given
        RegisterClientRequest request = RegisterClientRequest.builder()
                .companyName("Tech Corp")
                .build();

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(clientRepository.findById(userId)).thenReturn(Optional.of(client));

        // When & Then
        assertThatThrownBy(() -> clientServiceImpl.registerClient(userEmail, request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Client profile already exists");

        verify(userRepository).findByEmail(userEmail);
        verify(clientRepository).findById(userId);
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    @DisplayName("Should get client profile successfully")
    void testGetClientProfile_Success() {
        // Given
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(clientRepository.findById(userId)).thenReturn(Optional.of(client));

        // When
        ClientProfileResponse response = clientServiceImpl.getClientProfile(userEmail);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(userId);
        assertThat(response.getEmail()).isEqualTo("client@example.com");
        assertThat(response.getFullName()).isEqualTo("John Doe");
        assertThat(response.getCompanyName()).isEqualTo("Tech Corp");

        verify(userRepository).findByEmail(userEmail);
        verify(clientRepository).findById(userId);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when client profile not found")
    void testGetClientProfile_NotFound() {
        // Given
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));

        // When & Then
        assertThatThrownBy(() -> clientServiceImpl.getClientProfile(userEmail))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Client profile not found");

        verify(userRepository).findByEmail(userEmail);
        verify(clientRepository).findById(userId);
    }

    @Test
    @DisplayName("Should update client profile successfully")
    void testUpdateClientProfile_Success() {
        // Given
        UpdateClientProfileRequest request = UpdateClientProfileRequest.builder()
                .companyName("Updated Corp")
                .industry("Software")
                .companySize(CompanySize.SIZE_51_200)
                .paymentMethods(List.of())
                .build();

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(clientRepository.findById(userId)).thenReturn(Optional.of(client));
        when(clientRepository.save(any(Client.class))).thenReturn(client);

        // When
        ClientProfileResponse response = clientServiceImpl.updateClientProfile(userEmail, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(client.getCompanyName()).isEqualTo("Updated Corp");
        assertThat(client.getIndustry()).isEqualTo("Software");
        assertThat(client.getCompanySize()).isEqualTo(CompanySize.SIZE_51_200);

        verify(userRepository).findByEmail(userEmail);
        verify(clientRepository).findById(userId);
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    @DisplayName("Should update only non-null fields")
    void testUpdateClientProfile_PartialUpdate() {
        // Given
        UpdateClientProfileRequest request = UpdateClientProfileRequest.builder()
                .companyName("Partial Update")
                .build();

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(clientRepository.findById(userId)).thenReturn(Optional.of(client));
        when(clientRepository.save(any(Client.class))).thenReturn(client);

        // When
        clientServiceImpl.updateClientProfile(userEmail, request);

        // Then
        assertThat(client.getCompanyName()).isEqualTo("Partial Update");
        assertThat(client.getIndustry()).isEqualTo("Technology"); // Unchanged
        assertThat(client.getCompanySize()).isEqualTo(CompanySize.SIZE_11_50); // Unchanged

        verify(clientRepository).save(any(Client.class));
    }

    @Test
    @DisplayName("Should get client stats successfully")
    void testGetClientStats_Success() {
        // Given
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(clientRepository.findById(userId)).thenReturn(Optional.of(client));

        // When
        ClientStatsResponse response = clientServiceImpl.getClientStats(userEmail);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getClientId()).isEqualTo(userId);
        assertThat(response.getCompanyName()).isEqualTo("Tech Corp");
        assertThat(response.getTotalProjects()).isEqualTo(0);
        assertThat(response.getTotalSpent()).isEqualByComparingTo("0");

        verify(userRepository).findByEmail(userEmail);
        verify(clientRepository).findById(userId);
    }

    @Test
    @DisplayName("Should delete client successfully")
    void testDeleteClient_Success() {
        // Given
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        doNothing().when(clientRepository).deleteById(userId);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        clientServiceImpl.deleteClient(userEmail);

        // Then
        assertThat(user.getIsActive()).isFalse();

        verify(userRepository).findByEmail(userEmail);
        verify(clientRepository).deleteById(userId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent user")
    void testDeleteClient_UserNotFound() {
        // Given
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> clientServiceImpl.deleteClient(userEmail))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");

        verify(userRepository).findByEmail(userEmail);
        verify(clientRepository, never()).deleteById(userId);
    }
}
