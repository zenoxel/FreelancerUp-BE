package com.FreelancerUp.config;

import com.FreelancerUp.cache.RedisCacheService;
import com.FreelancerUp.exception.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Rate Limiting Interceptor for sensitive endpoints.
 *
 * Rate Limits:
 * - Login: 5 attempts per 15 minutes
 * - Payment: 10 requests per minute
 * - Bid submission: 20 per hour
 * - General API: 100 requests per minute
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RedisCacheService redisCacheService;

    // Rate limit configurations
    private static final int LOGIN_MAX_ATTEMPTS = 5;
    private static final long LOGIN_WINDOW_MINUTES = 15;

    private static final int PAYMENT_MAX_REQUESTS = 10;
    private static final long PAYMENT_WINDOW_MINUTES = 1;

    private static final int BID_MAX_REQUESTS = 20;
    private static final long BID_WINDOW_HOURS = 1;

    private static final int GENERAL_MAX_REQUESTS = 100;
    private static final long GENERAL_WINDOW_MINUTES = 1;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientIp = getClientIp(request);
        String requestUri = request.getRequestURI();
        String method = request.getMethod();

        // Skip rate limiting for non-mutating operations
        if ("GET".equals(method) || "HEAD".equals(method) || "OPTIONS".equals(method)) {
            return true;
        }

        // Apply rate limiting based on endpoint
        if (requestUri.contains("/auth/login")) {
            checkRateLimit(clientIp, "login", LOGIN_MAX_ATTEMPTS, LOGIN_WINDOW_MINUTES, TimeUnit.MINUTES);
        } else if (requestUri.contains("/payments/")) {
            checkRateLimit(clientIp, "payment", PAYMENT_MAX_REQUESTS, PAYMENT_WINDOW_MINUTES, TimeUnit.MINUTES);
        } else if (requestUri.contains("/bids")) {
            checkRateLimit(clientIp, "bid", BID_MAX_REQUESTS, BID_WINDOW_HOURS, TimeUnit.HOURS);
        } else {
            checkRateLimit(clientIp, "general", GENERAL_MAX_REQUESTS, GENERAL_WINDOW_MINUTES, TimeUnit.MINUTES);
        }

        return true;
    }

    /**
     * Check rate limit for a given key.
     */
    private void checkRateLimit(String clientIp, String endpoint, int maxRequests, long window, TimeUnit timeUnit) throws IOException {
        String key = String.format("rate_limit:%s:%s", endpoint, clientIp);

        Long currentCount = redisCacheService.getCounter(key);

        if (currentCount == null) {
            // First request in window
            redisCacheService.increment(key);
            redisCacheService.expire(key, window, timeUnit);
            log.debug("Rate limit initialized for {} from {}: {}/{}", endpoint, clientIp, 1, maxRequests);
        } else if (currentCount < maxRequests) {
            // Increment counter
            redisCacheService.increment(key);
            log.debug("Rate limit for {} from {}: {}/{}", endpoint, clientIp, currentCount + 1, maxRequests);
        } else {
            // Rate limit exceeded
            log.warn("Rate limit exceeded for {} from {}: {}/{}", endpoint, clientIp, currentCount, maxRequests);
            throw new RateLimitExceededException(
                    String.format("Rate limit exceeded for %s. Maximum %d requests per %d %s.",
                            endpoint, maxRequests, window, timeUnit.toString().toLowerCase())
            );
        }
    }

    /**
     * Extract client IP from request.
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // Handle multiple IPs in X-Forwarded-For
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
