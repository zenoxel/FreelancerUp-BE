package com.FreelancerUp.cache;

import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class RedisCacheService {

    public String get(String key) {
        // TODO: Implement in Phase 11 - Redis Caching
        return null;
    }

    public void set(String key, String value, long timeout, java.util.concurrent.TimeUnit unit) {
        // TODO: Implement in Phase 11 - Redis Caching
    }

    public void delete(String key) {
        // TODO: Implement in Phase 11 - Redis Caching
    }

    public Set<String> keys(String pattern) {
        // TODO: Implement in Phase 11 - Redis Caching
        return Set.of();
    }
}
