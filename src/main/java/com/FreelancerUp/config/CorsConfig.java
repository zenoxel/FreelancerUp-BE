package com.FreelancerUp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

/**
 * CORS Configuration for FreelancerUp API.
 *
 * Allows cross-origin requests from configured frontend origins.
 */
@Configuration
public class CorsConfig {

    // Allowed origins for frontend applications
    private static final List<String> ALLOWED_ORIGINS = Arrays.asList(
            "http://localhost:3000",
            "http://localhost:3001",
            "http://localhost:5173",  // Vite default
            "http://127.0.0.1:3000",
            "http://127.0.0.1:3001",
            "http://127.0.0.1:5173"
    );

    // Allowed HTTP methods
    private static final List<String> ALLOWED_METHODS = Arrays.asList(
            "GET",
            "POST",
            "PUT",
            "PATCH",
            "DELETE",
            "OPTIONS"
    );

    // Allowed headers
    private static final List<String> ALLOWED_HEADERS = Arrays.asList(
            "Authorization",
            "Content-Type",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers",
            "X-Requested-With",
            "X-Refresh-Token"
    );

    // Exposed headers
    private static final List<String> EXPOSED_HEADERS = Arrays.asList(
            "Authorization",
            "X-Refresh-Token"
    );

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Allow credentials (cookies, authorization headers)
        config.setAllowCredentials(true);

        // Allowed origins
        config.setAllowedOrigins(ALLOWED_ORIGINS);

        // Allowed methods
        config.setAllowedMethods(ALLOWED_METHODS);

        // Allowed headers
        config.setAllowedHeaders(ALLOWED_HEADERS);

        // Exposed headers
        config.setExposedHeaders(EXPOSED_HEADERS);

        // Cache preflight response for 1 hour
        config.setMaxAge(3600L);

        // Apply CORS configuration to all endpoints
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
