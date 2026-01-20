package com.FreelancerUp.feature.bid.controller;

import com.FreelancerUp.feature.bid.dto.request.SubmitBidRequest;
import com.FreelancerUp.feature.bid.dto.response.BidResponse;
import com.FreelancerUp.feature.bid.service.BidService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Bid", description = "Bid management APIs")
@SecurityRequirement(name = "bearerAuth")
public class BidController {

    private final BidService bidService;

    @PostMapping(value = "/projects/{projectId}/bids", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('FREELANCER')")
    @Operation(summary = "Submit a bid", description = "Freelancer can submit a bid on a project")
    public ResponseEntity<BidResponse> submitBid(
            @Parameter(description = "Project ID", required = true)
            @PathVariable String projectId,
            @Valid @RequestBody SubmitBidRequest request) {
        // TODO: Get freelancerId from authentication context in Phase 12
        log.info("REST request to submit bid for project: {}", projectId);
        throw new UnsupportedOperationException("Will be implemented after security configuration");
    }

    @GetMapping("/projects/{projectId}/bids")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "List bids for project", description = "Client can view all bids on their project")
    public ResponseEntity<List<BidResponse>> getBidsForProject(
            @Parameter(description = "Project ID", required = true)
            @PathVariable String projectId) {
        log.info("REST request to get bids for project: {}", projectId);
        List<BidResponse> response = bidService.getBidsForProject(projectId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/bids/{bidId}/accept")
    @PreAuthorize("hasRole('CLIENT') and @bidSecurity.isOwner(#bidId, authentication)")
    @Operation(summary = "Accept a bid", description = "Client can accept a bid on their project")
    public ResponseEntity<BidResponse> acceptBid(
            @Parameter(description = "Bid ID", required = true)
            @PathVariable String bidId) {
        // TODO: Get clientId from authentication context in Phase 12
        log.info("REST request to accept bid: {}", bidId);
        throw new UnsupportedOperationException("Will be implemented after security configuration");
    }

    @PatchMapping("/bids/{bidId}/reject")
    @PreAuthorize("hasRole('CLIENT') and @bidSecurity.isOwner(#bidId, authentication)")
    @Operation(summary = "Reject a bid", description = "Client can reject a bid on their project")
    public ResponseEntity<BidResponse> rejectBid(
            @Parameter(description = "Bid ID", required = true)
            @PathVariable String bidId) {
        // TODO: Get clientId from authentication context in Phase 12
        log.info("REST request to reject bid: {}", bidId);
        throw new UnsupportedOperationException("Will be implemented after security configuration");
    }

    @DeleteMapping("/bids/{bidId}")
    @PreAuthorize("hasRole('FREELANCER') and @bidSecurity.isBidOwner(#bidId, authentication)")
    @Operation(summary = "Withdraw bid", description = "Freelancer can withdraw their pending bid")
    public ResponseEntity<Void> withdrawBid(
            @Parameter(description = "Bid ID", required = true)
            @PathVariable String bidId) {
        // TODO: Get freelancerId from authentication context in Phase 12
        log.info("REST request to withdraw bid: {}", bidId);
        throw new UnsupportedOperationException("Will be implemented after security configuration");
    }

    @GetMapping("/freelancers/{freelancerId}/bids")
    @PreAuthorize("hasRole('FREELANCER') and #freelancerId == authentication.principal.id")
    @Operation(summary = "Get freelancer bids", description = "Freelancer can view their bid history")
    public ResponseEntity<List<BidResponse>> getFreelancerBids(
            @Parameter(description = "Freelancer ID", required = true)
            @PathVariable UUID freelancerId) {
        log.info("REST request to get bids for freelancer: {}", freelancerId);
        List<BidResponse> response = bidService.getFreelancerBids(freelancerId);
        return ResponseEntity.ok(response);
    }
}
