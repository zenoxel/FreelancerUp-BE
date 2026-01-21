package com.FreelancerUp.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis Cache Service for FreelancerUp.
 *
 * Cache Patterns:
 * 1. Session Caching - session:{refreshToken} (TTL: 7 days)
 * 2. Profile Caching - profile:{userId} (TTL: 1 hour)
 * 3. Project Listing - projects:list:{filters_hash} (TTL: 15 minutes)
 * 4. Online Users - online:users (Set)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    // Cache TTL constants
    private static final long SESSION_TTL_DAYS = 7;
    private static final long PROFILE_TTL_HOURS = 1;
    private static final long PROJECT_LIST_TTL_MINUTES = 15;

    // Key prefixes
    private static final String SESSION_PREFIX = "session:";
    private static final String PROFILE_PREFIX = "profile:";
    private static final String PROJECT_LIST_PREFIX = "projects:list:";
    private static final String ONLINE_USERS_KEY = "online:users";

    // ========== Generic Cache Operations ==========

    /**
     * Get value from cache.
     */
    public String get(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            return value != null ? value.toString() : null;
        } catch (Exception e) {
            log.error("Error getting value from Redis for key: {}", key, e);
            return null;
        }
    }

    /**
     * Get object from cache.
     */
    public <T> T get(String key, Class<T> clazz) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                return null;
            }
            if (clazz.isInstance(value)) {
                return clazz.cast(value);
            }
            // Try to convert using ObjectMapper
            return objectMapper.convertValue(value, clazz);
        } catch (Exception e) {
            log.error("Error getting object from Redis for key: {}", key, e);
            return null;
        }
    }

    /**
     * Set value in cache with timeout.
     */
    public void set(String key, String value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
            log.debug("Set cache key: {} with TTL: {} {}", key, timeout, unit);
        } catch (Exception e) {
            log.error("Error setting value in Redis for key: {}", key, e);
        }
    }

    /**
     * Set object in cache with timeout.
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
            log.debug("Set cache key: {} with TTL: {} {}", key, timeout, unit);
        } catch (Exception e) {
            log.error("Error setting object in Redis for key: {}", key, e);
        }
    }

    /**
     * Delete key from cache.
     */
    public void delete(String key) {
        try {
            redisTemplate.delete(key);
            log.debug("Deleted cache key: {}", key);
        } catch (Exception e) {
            log.error("Error deleting key from Redis: {}", key, e);
        }
    }

    /**
     * Delete multiple keys from cache.
     */
    public void delete(Collection<String> keys) {
        try {
            redisTemplate.delete(keys);
            log.debug("Deleted {} cache keys", keys.size());
        } catch (Exception e) {
            log.error("Error deleting keys from Redis", e);
        }
    }

    /**
     * Delete keys by pattern.
     */
    public void deleteByPattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("Deleted {} keys matching pattern: {}", keys.size(), pattern);
            }
        } catch (Exception e) {
            log.error("Error deleting keys by pattern: {}", pattern, e);
        }
    }

    /**
     * Check if key exists.
     */
    public boolean exists(String key) {
        try {
            Boolean result = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("Error checking key existence: {}", key, e);
            return false;
        }
    }

    /**
     * Set TTL for existing key.
     */
    public void expire(String key, long timeout, TimeUnit unit) {
        try {
            redisTemplate.expire(key, timeout, unit);
            log.debug("Set TTL for key: {} to {} {}", key, timeout, unit);
        } catch (Exception e) {
            log.error("Error setting TTL for key: {}", key, e);
        }
    }

    /**
     * Get all keys matching pattern.
     */
    public Set<String> keys(String pattern) {
        try {
            return redisTemplate.keys(pattern);
        } catch (Exception e) {
            log.error("Error getting keys by pattern: {}", pattern, e);
            return Set.of();
        }
    }

    // ========== Session Caching ==========

    /**
     * Cache user session with refresh token.
     * Key: session:{refreshToken}
     * Value: {userId, email, role, createdAt}
     * TTL: 7 days
     */
    public void cacheSession(String refreshToken, UUID userId, String email, String role) {
        String key = SESSION_PREFIX + refreshToken;
        Map<String, Object> sessionData = new HashMap<>();
        sessionData.put("userId", userId.toString());
        sessionData.put("email", email);
        sessionData.put("role", role);
        sessionData.put("createdAt", System.currentTimeMillis());

        set(key, sessionData, SESSION_TTL_DAYS, TimeUnit.DAYS);
        log.info("Cached session for user: {}", userId);
    }

    /**
     * Get session data by refresh token.
     */
    public Map<String, Object> getSession(String refreshToken) {
        String key = SESSION_PREFIX + refreshToken;
        return get(key, new TypeReference<Map<String, Object>>() {});
    }

    /**
     * Delete session (logout).
     */
    public void deleteSession(String refreshToken) {
        String key = SESSION_PREFIX + refreshToken;
        delete(key);
        log.info("Deleted session for refresh token");
    }

    // ========== Profile Caching ==========

    /**
     * Cache user profile.
     * Key: profile:{userId}
     * TTL: 1 hour
     */
    public void cacheProfile(UUID userId, Object profileData) {
        String key = PROFILE_PREFIX + userId;
        set(key, profileData, PROFILE_TTL_HOURS, TimeUnit.HOURS);
        log.debug("Cached profile for user: {}", userId);
    }

    /**
     * Get cached user profile.
     */
    public <T> T getCachedProfile(UUID userId, Class<T> clazz) {
        String key = PROFILE_PREFIX + userId;
        return get(key, clazz);
    }

    /**
     * Invalidate cached profile.
     */
    public void invalidateProfile(UUID userId) {
        String key = PROFILE_PREFIX + userId;
        delete(key);
        log.debug("Invalidated profile cache for user: {}", userId);
    }

    // ========== Project Listing Cache ==========

    /**
     * Cache project listing result.
     * Key: projects:list:{filters_hash}
     * TTL: 15 minutes
     */
    public void cacheProjectList(String filtersHash, Object projectIds) {
        String key = PROJECT_LIST_PREFIX + filtersHash;
        set(key, projectIds, PROJECT_LIST_TTL_MINUTES, TimeUnit.MINUTES);
        log.debug("Cached project list for filters: {}", filtersHash);
    }

    /**
     * Get cached project listing.
     */
    public <T> T getCachedProjectList(String filtersHash, Class<T> clazz) {
        String key = PROJECT_LIST_PREFIX + filtersHash;
        return get(key, clazz);
    }

    /**
     * Invalidate all project listing caches.
     */
    public void invalidateProjectListings() {
        deleteByPattern(PROJECT_LIST_PREFIX + "*");
        log.debug("Invalidated all project listing caches");
    }

    // ========== Online Users Tracking ==========

    /**
     * Add user to online set.
     */
    public void addOnlineUser(UUID userId) {
        try {
            redisTemplate.opsForSet().add(ONLINE_USERS_KEY, userId.toString());
            log.debug("Added user to online set: {}", userId);
        } catch (Exception e) {
            log.error("Error adding online user: {}", userId, e);
        }
    }

    /**
     * Remove user from online set.
     */
    public void removeOnlineUser(UUID userId) {
        try {
            redisTemplate.opsForSet().remove(ONLINE_USERS_KEY, userId.toString());
            log.debug("Removed user from online set: {}", userId);
        } catch (Exception e) {
            log.error("Error removing online user: {}", userId, e);
        }
    }

    /**
     * Get all online users.
     */
    public Set<UUID> getOnlineUsers() {
        try {
            Set<Object> members = redisTemplate.opsForSet().members(ONLINE_USERS_KEY);
            if (members == null || members.isEmpty()) {
                return Set.of();
            }
            return members.stream()
                    .map(obj -> UUID.fromString(obj.toString()))
                    .collect(java.util.stream.Collectors.toSet());
        } catch (Exception e) {
            log.error("Error getting online users", e);
            return Set.of();
        }
    }

    /**
     * Check if user is online.
     */
    public boolean isUserOnline(UUID userId) {
        try {
            Boolean isMember = redisTemplate.opsForSet().isMember(ONLINE_USERS_KEY, userId.toString());
            return Boolean.TRUE.equals(isMember);
        } catch (Exception e) {
            log.error("Error checking online status for user: {}", userId, e);
            return false;
        }
    }

    /**
     * Get online users count.
     */
    public long getOnlineUsersCount() {
        try {
            Long size = redisTemplate.opsForSet().size(ONLINE_USERS_KEY);
            return size != null ? size : 0L;
        } catch (Exception e) {
            log.error("Error getting online users count", e);
            return 0L;
        }
    }

    // ========== Helper Methods ==========

    /**
     * Helper method for generic type reference.
     */
    private <T> T get(String key, TypeReference<T> typeRef) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                return null;
            }
            return objectMapper.convertValue(value, typeRef);
        } catch (Exception e) {
            log.error("Error getting object from Redis for key: {}", key, e);
            return null;
        }
    }

    /**
     * Increment counter.
     */
    public Long increment(String key) {
        try {
            return redisTemplate.opsForValue().increment(key);
        } catch (Exception e) {
            log.error("Error incrementing key: {}", key, e);
            return 0L;
        }
    }

    /**
     * Increment counter by delta.
     */
    public Long increment(String key, long delta) {
        try {
            return redisTemplate.opsForValue().increment(key, delta);
        } catch (Exception e) {
            log.error("Error incrementing key: {} by: {}", key, delta, e);
            return 0L;
        }
    }

    /**
     * Get counter value.
     */
    public Long getCounter(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value instanceof Number) {
                return ((Number) value).longValue();
            }
            return value != null ? Long.parseLong(value.toString()) : 0L;
        } catch (Exception e) {
            log.error("Error getting counter for key: {}", key, e);
            return 0L;
        }
    }
}
