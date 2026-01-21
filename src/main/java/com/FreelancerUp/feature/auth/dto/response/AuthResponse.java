package com.FreelancerUp.feature.auth.dto.response;

import com.FreelancerUp.model.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private User user;
    private String accessToken;
    private String refreshToken;
}
