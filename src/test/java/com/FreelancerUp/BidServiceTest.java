package com.FreelancerUp;

import com.FreelancerUp.exception.BadRequestException;
import com.FreelancerUp.exception.ResourceNotFoundException;
import com.FreelancerUp.feature.bid.dto.request.SubmitBidRequest;
import com.FreelancerUp.feature.bid.dto.response.BidResponse;
import com.FreelancerUp.feature.bid.service.impl.BidServiceImpl;
import com.FreelancerUp.feature.contract.service.ContractService;
import com.FreelancerUp.model.document.Bid;
import com.FreelancerUp.model.document.Project;
import com.FreelancerUp.model.entity.User;
import com.FreelancerUp.model.enums.BidStatus;
import com.FreelancerUp.model.enums.ProjectStatus;
import com.FreelancerUp.model.enums.ProjectType;
import com.FreelancerUp.feature.bid.repository.BidRepository;
import com.FreelancerUp.feature.project.repository.ProjectRepository;
import com.FreelancerUp.feature.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Bid Service Tests")
class BidServiceTest {

    @Mock
    private BidRepository bidRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ContractService contractService;

    private BidServiceImpl bidServiceImpl;

    private UUID clientId;
    private UUID freelancerId;
    private String projectId;
    private String bidId;
    private User client;
    private User freelancer;
    private Project project;
    private Bid bid;

    @BeforeEach
    void setUp() {
        bidServiceImpl = new BidServiceImpl(bidRepository, projectRepository, userRepository, contractService);

        clientId = UUID.randomUUID();
        freelancerId = UUID.randomUUID();
        projectId = "project123";
        bidId = "bid123";

        client = User.builder()
                .id(clientId)
                .email("client@example.com")
                .fullName("Client User")
                .avatarUrl("http://example.com/client.jpg")
                .build();

        freelancer = User.builder()
                .id(freelancerId)
                .email("freelancer@example.com")
                .fullName("Freelancer User")
                .avatarUrl("http://example.com/freelancer.jpg")
                .build();

        project = Project.builder()
                .id(projectId)
                .clientId(clientId.toString())
                .title("Build Mobile App")
                .status(ProjectStatus.OPEN)
                .type(ProjectType.FIXED_PRICE)
                .build();

        bid = Bid.builder()
                .id(bidId)
                .projectId(projectId)
                .freelancerId(freelancerId.toString())
                .proposal("I can build this app for you")
                .price(BigDecimal.valueOf(5000))
                .estimatedDuration(60)
                .status(BidStatus.PENDING)
                .submittedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should submit bid successfully")
    void testSubmitBid_Success() {
        // Given
        SubmitBidRequest request = SubmitBidRequest.builder()
                .proposal("I can build this app for you")
                .price(BigDecimal.valueOf(5000))
                .estimatedDuration(60)
                .build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(bidRepository.findByProjectIdAndFreelancerId(projectId, freelancerId.toString()))
                .thenReturn(Optional.empty());
        when(userRepository.findById(freelancerId)).thenReturn(Optional.of(freelancer));
        when(bidRepository.save(any(Bid.class))).thenReturn(bid);

        // When
        BidResponse response = bidServiceImpl.submitBid(projectId, freelancerId, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(bidId);
        assertThat(response.getProposal()).isEqualTo("I can build this app for you");

        verify(projectRepository).findById(projectId);
        verify(bidRepository).save(any(Bid.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when project not found")
    void testSubmitBid_ProjectNotFound() {
        // Given
        SubmitBidRequest request = SubmitBidRequest.builder()
                .proposal("Proposal")
                .price(BigDecimal.valueOf(1000))
                .estimatedDuration(30)
                .build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> bidServiceImpl.submitBid(projectId, freelancerId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Project not found");

        verify(projectRepository).findById(projectId);
        verify(bidRepository, never()).save(any(Bid.class));
    }

    @Test
    @DisplayName("Should throw BadRequestException when project is not open")
    void testSubmitBid_ProjectNotOpen() {
        // Given
        project.setStatus(ProjectStatus.IN_PROGRESS);
        SubmitBidRequest request = SubmitBidRequest.builder()
                .proposal("Proposal")
                .price(BigDecimal.valueOf(1000))
                .build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        // When & Then
        assertThatThrownBy(() -> bidServiceImpl.submitBid(projectId, freelancerId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Can only bid on open projects");

        verify(bidRepository, never()).save(any(Bid.class));
    }

    @Test
    @DisplayName("Should throw BadRequestException when freelancer already bid")
    void testSubmitBid_AlreadyBidded() {
        // Given
        SubmitBidRequest request = SubmitBidRequest.builder()
                .proposal("Proposal")
                .price(BigDecimal.valueOf(1000))
                .build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(bidRepository.findByProjectIdAndFreelancerId(projectId, freelancerId.toString()))
                .thenReturn(Optional.of(bid));

        // When & Then
        assertThatThrownBy(() -> bidServiceImpl.submitBid(projectId, freelancerId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("You have already placed a bid on this project");

        verify(bidRepository, never()).save(any(Bid.class));
    }

    @Test
    @DisplayName("Should get bids for project successfully")
    void testGetBidsForProject_Success() {
        // Given
        when(projectRepository.existsById(projectId)).thenReturn(true);
        when(bidRepository.findByProjectId(projectId)).thenReturn(List.of(bid));
        when(userRepository.findById(freelancerId)).thenReturn(Optional.of(freelancer));

        // When
        List<BidResponse> response = bidServiceImpl.getBidsForProject(projectId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response).hasSize(1);
        assertThat(response.get(0).getId()).isEqualTo(bidId);

        verify(projectRepository).existsById(projectId);
        verify(bidRepository).findByProjectId(projectId);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when project not found while getting bids")
    void testGetBidsForProject_ProjectNotFound() {
        // Given
        when(projectRepository.existsById(projectId)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> bidServiceImpl.getBidsForProject(projectId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Project not found");

        verify(projectRepository).existsById(projectId);
        verify(bidRepository, never()).findByProjectId(anyString());
    }

    @Test
    @DisplayName("Should accept bid successfully")
    void testAcceptBid_Success() {
        // Given
        when(bidRepository.findById(bidId)).thenReturn(Optional.of(bid));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(userRepository.findById(freelancerId)).thenReturn(Optional.of(freelancer));
        when(bidRepository.save(any(Bid.class))).thenReturn(bid);
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        doNothing().when(contractService).createContract(any(), any(), any());

        // When
        BidResponse response = bidServiceImpl.acceptBid(bidId, clientId);

        // Then
        assertThat(response).isNotNull();
        assertThat(bid.getStatus()).isEqualTo(BidStatus.ACCEPTED);
        assertThat(bid.getRespondedAt()).isNotNull();

        verify(bidRepository).findById(bidId);
        verify(bidRepository).save(any(Bid.class));
        verify(contractService).createContract(eq(projectId), eq(clientId), eq(freelancerId));
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    @DisplayName("Should throw BadRequestException when accepting bid for non-owned project")
    void testAcceptBid_NotOwner() {
        // Given
        UUID differentClientId = UUID.randomUUID();
        when(bidRepository.findById(bidId)).thenReturn(Optional.of(bid));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        // When & Then
        assertThatThrownBy(() -> bidServiceImpl.acceptBid(bidId, differentClientId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("You can only accept bids for your own projects");

        verify(bidRepository, never()).save(any(Bid.class));
    }

    @Test
    @DisplayName("Should throw BadRequestException when accepting non-pending bid")
    void testAcceptBid_NotPending() {
        // Given
        bid.setStatus(BidStatus.ACCEPTED);
        when(bidRepository.findById(bidId)).thenReturn(Optional.of(bid));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        // When & Then
        assertThatThrownBy(() -> bidServiceImpl.acceptBid(bidId, clientId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Can only accept pending bids");

        verify(bidRepository, never()).save(any(Bid.class));
    }

    @Test
    @DisplayName("Should reject bid successfully")
    void testRejectBid_Success() {
        // Given
        when(bidRepository.findById(bidId)).thenReturn(Optional.of(bid));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(userRepository.findById(freelancerId)).thenReturn(Optional.of(freelancer));
        when(bidRepository.save(any(Bid.class))).thenReturn(bid);

        // When
        BidResponse response = bidServiceImpl.rejectBid(bidId, clientId);

        // Then
        assertThat(response).isNotNull();
        assertThat(bid.getStatus()).isEqualTo(BidStatus.REJECTED);
        assertThat(bid.getRespondedAt()).isNotNull();

        verify(bidRepository).save(any(Bid.class));
    }

    @Test
    @DisplayName("Should withdraw bid successfully")
    void testWithdrawBid_Success() {
        // Given
        when(bidRepository.findById(bidId)).thenReturn(Optional.of(bid));
        doNothing().when(bidRepository).deleteById(bidId);

        // When
        bidServiceImpl.withdrawBid(bidId, freelancerId);

        // Then
        verify(bidRepository).findById(bidId);
        verify(bidRepository).deleteById(bidId);
    }

    @Test
    @DisplayName("Should throw BadRequestException when withdrawing non-owned bid")
    void testWithdrawBid_NotOwner() {
        // Given
        UUID differentFreelancerId = UUID.randomUUID();
        when(bidRepository.findById(bidId)).thenReturn(Optional.of(bid));

        // When & Then
        assertThatThrownBy(() -> bidServiceImpl.withdrawBid(bidId, differentFreelancerId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("You can only withdraw your own bids");

        verify(bidRepository, never()).deleteById(anyString());
    }

    @Test
    @DisplayName("Should throw BadRequestException when withdrawing non-pending bid")
    void testWithdrawBid_NotPending() {
        // Given
        bid.setStatus(BidStatus.ACCEPTED);
        when(bidRepository.findById(bidId)).thenReturn(Optional.of(bid));

        // When & Then
        assertThatThrownBy(() -> bidServiceImpl.withdrawBid(bidId, freelancerId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Can only withdraw pending bids");

        verify(bidRepository, never()).deleteById(anyString());
    }

    @Test
    @DisplayName("Should get freelancer bids successfully")
    void testGetFreelancerBids_Success() {
        // Given
        when(bidRepository.findByFreelancerId(freelancerId.toString())).thenReturn(List.of(bid));
        when(userRepository.findById(freelancerId)).thenReturn(Optional.of(freelancer));

        // When
        List<BidResponse> response = bidServiceImpl.getFreelancerBids(freelancerId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response).hasSize(1);
        assertThat(response.get(0).getId()).isEqualTo(bidId);

        verify(bidRepository).findByFreelancerId(freelancerId.toString());
    }
}
