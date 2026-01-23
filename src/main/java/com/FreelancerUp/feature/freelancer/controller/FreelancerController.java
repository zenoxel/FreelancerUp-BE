package com.FreelancerUp.feature.freelancer.controller;

import com.FreelancerUp.feature.freelancer.dto.request.RegisterFreelancerRequest;
import com.FreelancerUp.feature.freelancer.dto.request.UpdateFreelancerProfileRequest;
import com.FreelancerUp.feature.freelancer.dto.response.FreelancerProfileResponse;
import com.FreelancerUp.feature.freelancer.dto.response.FreelancerStatsResponse;
import com.FreelancerUp.feature.freelancer.service.FreelancerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/freelancers", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Freelancer", description = "Freelancer Management APIs")
public class FreelancerController {

    private final FreelancerService freelancerService;

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Register as freelancer")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<FreelancerProfileResponse> registerFreelancer(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody RegisterFreelancerRequest request
    ) {
        String email = userDetails.getUsername();
        FreelancerProfileResponse response = freelancerService.registerFreelancer(email, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/profile")
    @Operation(summary = "Get freelancer profile")
    @PreAuthorize("hasRole('FREELANCER')")
    public ResponseEntity<FreelancerProfileResponse> getFreelancerProfile(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails.getUsername();
        FreelancerProfileResponse response = freelancerService.getFreelancerProfile(email);
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/profile", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update freelancer profile")
    @PreAuthorize("hasRole('FREELANCER')")
    public ResponseEntity<FreelancerProfileResponse> updateFreelancerProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateFreelancerProfileRequest request
    ) {
        String email = userDetails.getUsername();
        FreelancerProfileResponse response = freelancerService.updateFreelancerProfile(email, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    @Operation(summary = "Get freelancer statistics")
    @PreAuthorize("hasRole('FREELANCER')")
    public ResponseEntity<FreelancerStatsResponse> getFreelancerStats(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails.getUsername();
        FreelancerStatsResponse response = freelancerService.getFreelancerStats(email);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/profile")
    @Operation(summary = "Delete freelancer profile")
    @PreAuthorize("hasRole('FREELANCER')")
    public ResponseEntity<Void> deleteFreelancer(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails.getUsername();
        freelancerService.deleteFreelancer(email);
        return ResponseEntity.noContent().build();
    }
}
