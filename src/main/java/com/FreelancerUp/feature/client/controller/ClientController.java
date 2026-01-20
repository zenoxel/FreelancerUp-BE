package com.FreelancerUp.feature.client.controller;

import com.FreelancerUp.feature.client.dto.request.RegisterClientRequest;
import com.FreelancerUp.feature.client.dto.request.UpdateClientProfileRequest;
import com.FreelancerUp.feature.client.dto.response.ClientProfileResponse;
import com.FreelancerUp.feature.client.dto.response.ClientStatsResponse;
import com.FreelancerUp.feature.client.service.ClientService;
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

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/clients", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Client", description = "Client Management APIs")
public class ClientController {

    private final ClientService clientService;

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Register as client")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ClientProfileResponse> registerClient(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody RegisterClientRequest request
    ) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        ClientProfileResponse response = clientService.registerClient(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/profile")
    @Operation(summary = "Get client profile")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ClientProfileResponse> getClientProfile(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        ClientProfileResponse response = clientService.getClientProfile(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/profile", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update client profile")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ClientProfileResponse> updateClientProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateClientProfileRequest request
    ) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        ClientProfileResponse response = clientService.updateClientProfile(userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    @Operation(summary = "Get client statistics")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ClientStatsResponse> getClientStats(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        ClientStatsResponse response = clientService.getClientStats(userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/profile")
    @Operation(summary = "Delete client profile")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Void> deleteClient(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        clientService.deleteClient(userId);
        return ResponseEntity.noContent().build();
    }
}
