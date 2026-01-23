package com.FreelancerUp.config;

import com.FreelancerUp.cache.RedisCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Cleanup stale rate limit keys on application startup.
 * This is useful for development environments where Redis persists between restarts.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitCleanupConfig implements ApplicationRunner {

    private final RedisCacheService redisCacheService;

    @Value("${app.rate-limit.cleanup-on-startup:false}")
    private boolean cleanupOnStartup;

    @Override
    public void run(ApplicationArguments args) {
        if (!cleanupOnStartup) {
            log.info("Rate limit cleanup on startup is disabled. Set app.rate-limit.cleanup-on-startup=true to enable.");
            return;
        }

        log.info("Cleaning up rate limit keys on startup...");

        try {
            // Clean up all rate limit keys
            Set<String> rateLimitKeys = redisCacheService.keys("rate_limit:*");

            if (rateLimitKeys == null || rateLimitKeys.isEmpty()) {
                log.info("No rate limit keys found to clean up.");
                return;
            }

            redisCacheService.delete(rateLimitKeys);
            log.info("Successfully cleaned up {} rate limit keys.", rateLimitKeys.size());

        } catch (Exception e) {
            log.error("Error cleaning up rate limit keys: {}", e.getMessage(), e);
        }
    }
}
