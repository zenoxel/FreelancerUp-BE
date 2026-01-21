package com.FreelancerUp.feature.bid.service.impl;

import com.FreelancerUp.exception.BadRequestException;
import com.FreelancerUp.exception.ResourceNotFoundException;
import com.FreelancerUp.feature.bid.dto.request.SubmitBidRequest;
import com.FreelancerUp.feature.bid.dto.response.BidResponse;
import com.FreelancerUp.feature.bid.service.BidService;
import com.FreelancerUp.feature.contract.service.ContractService;
import com.FreelancerUp.feature.project.repository.ProjectRepository;
import com.FreelancerUp.feature.user.repository.UserRepository;
import com.FreelancerUp.model.document.Bid;
import com.FreelancerUp.model.document.Project;
import com.FreelancerUp.model.entity.User;
import com.FreelancerUp.model.enums.BidStatus;
import com.FreelancerUp.model.enums.ProjectStatus;
import com.FreelancerUp.feature.bid.repository.BidRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BidServiceImpl implements BidService {

    private final BidRepository bidRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ContractService contractService;

    @Override
    public BidResponse submitBid(String projectId, UUID freelancerId, SubmitBidRequest request) {
        log.info("Submitting bid for project: {} by freelancer: {}", projectId, freelancerId);

        // Validate project exists
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        // Validate project is open
        if (project.getStatus() != ProjectStatus.OPEN) {
            throw new BadRequestException("Can only bid on open projects");
        }

        // Check if freelancer already bid on this project
        bidRepository.findByProjectIdAndFreelancerId(projectId, freelancerId.toString())
                .ifPresent(existingBid -> {
                    throw new BadRequestException("You have already placed a bid on this project");
                });

        // Validate freelancer exists
        User freelancer = userRepository.findById(freelancerId)
                .orElseThrow(() -> new ResourceNotFoundException("Freelancer not found"));

        // Create bid
        Bid bid = Bid.builder()
                .projectId(projectId)
                .freelancerId(freelancerId.toString())
                .proposal(request.getProposal())
                .price(request.getPrice())
                .estimatedDuration(request.getEstimatedDuration())
                .status(BidStatus.PENDING)
                .submittedAt(LocalDateTime.now())
                .build();

        bid = bidRepository.save(bid);

        log.info("Bid submitted successfully: {}", bid.getId());
        return convertToResponse(bid, freelancer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BidResponse> getBidsForProject(String projectId) {
        log.info("Fetching bids for project: {}", projectId);

        // Validate project exists
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found");
        }

        List<Bid> bids = bidRepository.findByProjectId(projectId);

        return bids.stream()
                .map(bid -> {
                    User freelancer = userRepository.findById(UUID.fromString(bid.getFreelancerId()))
                            .orElse(null);
                    return convertToResponse(bid, freelancer);
                })
                .collect(Collectors.toList());
    }

    @Override
    public BidResponse acceptBid(String bidId, UUID clientId) {
        log.info("Accepting bid: {} by client: {}", bidId, clientId);

        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new ResourceNotFoundException("Bid not found"));

        // Validate project ownership
        Project project = projectRepository.findById(bid.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        if (!project.getClientId().equals(clientId.toString())) {
            throw new BadRequestException("You can only accept bids for your own projects");
        }

        // Validate bid status
        if (bid.getStatus() != BidStatus.PENDING) {
            throw new BadRequestException("Can only accept pending bids");
        }

        // Validate project is still open
        if (project.getStatus() != ProjectStatus.OPEN) {
            throw new BadRequestException("Project is no longer open for bidding");
        }

        // Update bid status
        bid.setStatus(BidStatus.ACCEPTED);
        bid.setRespondedAt(LocalDateTime.now());
        bid = bidRepository.save(bid);

        // Auto-create contract when bid is accepted
        contractService.createContract(
                bid.getProjectId(),
                clientId,
                UUID.fromString(bid.getFreelancerId())
        );

        // Update project status to IN_PROGRESS
        project.setStatus(ProjectStatus.IN_PROGRESS);
        project.setFreelancerId(bid.getFreelancerId());
        project.setStartedAt(LocalDateTime.now());
        projectRepository.save(project);

        User freelancer = userRepository.findById(UUID.fromString(bid.getFreelancerId()))
                .orElseThrow(() -> new ResourceNotFoundException("Freelancer not found"));

        log.info("Bid accepted successfully: {}", bidId);
        return convertToResponse(bid, freelancer);
    }

    @Override
    public BidResponse rejectBid(String bidId, UUID clientId) {
        log.info("Rejecting bid: {} by client: {}", bidId, clientId);

        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new ResourceNotFoundException("Bid not found"));

        // Validate project ownership
        Project project = projectRepository.findById(bid.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        if (!project.getClientId().equals(clientId.toString())) {
            throw new BadRequestException("You can only reject bids for your own projects");
        }

        // Validate bid status
        if (bid.getStatus() != BidStatus.PENDING) {
            throw new BadRequestException("Can only reject pending bids");
        }

        // Update bid status
        bid.setStatus(BidStatus.REJECTED);
        bid.setRespondedAt(LocalDateTime.now());
        bid = bidRepository.save(bid);

        User freelancer = userRepository.findById(UUID.fromString(bid.getFreelancerId()))
                .orElse(null);

        log.info("Bid rejected successfully: {}", bidId);
        return convertToResponse(bid, freelancer);
    }

    @Override
    public void withdrawBid(String bidId, UUID freelancerId) {
        log.info("Withdrawing bid: {} by freelancer: {}", bidId, freelancerId);

        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new ResourceNotFoundException("Bid not found"));

        // Verify ownership
        if (!bid.getFreelancerId().equals(freelancerId.toString())) {
            throw new BadRequestException("You can only withdraw your own bids");
        }

        // Can only withdraw pending bids
        if (bid.getStatus() != BidStatus.PENDING) {
            throw new BadRequestException("Can only withdraw pending bids");
        }

        bidRepository.deleteById(bidId);

        log.info("Bid withdrawn successfully: {}", bidId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BidResponse> getFreelancerBids(UUID freelancerId) {
        log.info("Fetching bids for freelancer: {}", freelancerId);

        List<Bid> bids = bidRepository.findByFreelancerId(freelancerId.toString());

        return bids.stream()
                .map(bid -> {
                    User freelancer = userRepository.findById(UUID.fromString(bid.getFreelancerId()))
                            .orElse(null);
                    return convertToResponse(bid, freelancer);
                })
                .collect(Collectors.toList());
    }

    // Helper methods
    private BidResponse convertToResponse(Bid bid, User freelancer) {
        return BidResponse.builder()
                .id(bid.getId())
                .projectId(bid.getProjectId())
                .freelancerId(freelancer != null ? freelancer.getId() : null)
                .freelancerEmail(freelancer != null ? freelancer.getEmail() : null)
                .freelancerFullName(freelancer != null ? freelancer.getFullName() : null)
                .freelancerAvatarUrl(freelancer != null ? freelancer.getAvatarUrl() : null)
                .proposal(bid.getProposal())
                .price(bid.getPrice())
                .estimatedDuration(bid.getEstimatedDuration())
                .status(bid.getStatus())
                .submittedAt(bid.getSubmittedAt())
                .respondedAt(bid.getRespondedAt())
                .createdAt(bid.getCreatedAt())
                .updatedAt(bid.getUpdatedAt())
                .build();
    }
}
