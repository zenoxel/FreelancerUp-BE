package com.FreelancerUp.feature.bid.service;

import com.FreelancerUp.feature.bid.dto.request.SubmitBidRequest;
import com.FreelancerUp.feature.bid.dto.response.BidResponse;

import java.util.List;
import java.util.UUID;

public interface BidService {

    BidResponse submitBid(String projectId, UUID freelancerId, SubmitBidRequest request);

    List<BidResponse> getBidsForProject(String projectId);

    BidResponse acceptBid(String bidId, UUID clientId);

    BidResponse rejectBid(String bidId, UUID clientId);

    void withdrawBid(String bidId, UUID freelancerId);

    List<BidResponse> getFreelancerBids(UUID freelancerId);
}
