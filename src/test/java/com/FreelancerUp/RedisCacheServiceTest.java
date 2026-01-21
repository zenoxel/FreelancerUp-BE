package com.FreelancerUp;

import com.FreelancerUp.cache.RedisCacheService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RedisCacheService.
 *
 * Tests the cache operations:
 * 1. Generic cache operations (get, set, delete, exists)
 * 2. Session caching
 * 3. Profile caching
 * 4. Project listing cache
 * 5. Online users tracking
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Redis Cache Service Tests")
class RedisCacheServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private SetOperations<String, Object> setOperations;

    @InjectMocks
    private RedisCacheService redisCacheService;

    private ObjectMapper objectMapper;
    private UUID userId;
    private String refreshToken;
    private String testKey;
    private String testValue;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        // Use reflection to set the ObjectMapper
        try {
            var field = RedisCacheService.class.getDeclaredField("objectMapper");
            field.setAccessible(true);
            field.set(redisCacheService, objectMapper);
        } catch (Exception e) {
            // Ignore if field not accessible
        }

        userId = UUID.randomUUID();
        refreshToken = "test-refresh-token";
        testKey = "test:key";
        testValue = "test-value";

        // Setup common mock behaviors
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(redisTemplate.opsForSet()).thenReturn(setOperations);
    }

    @Test
    @DisplayName("Should get value from cache successfully")
    void testGet_Success() {
        when(valueOperations.get(testKey)).thenReturn(testValue);

        String result = redisCacheService.get(testKey);

        assertThat(result).isEqualTo(testValue);
        verify(valueOperations, times(1)).get(testKey);
    }

    @Test
    @DisplayName("Should return null when key not found")
    void testGet_NotFound() {
        when(valueOperations.get(testKey)).thenReturn(null);

        String result = redisCacheService.get(testKey);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should set value with TTL successfully")
    void testSet_Success() {
        redisCacheService.set(testKey, testValue, 1, TimeUnit.HOURS);

        verify(valueOperations, times(1)).set(testKey, testValue, 1, TimeUnit.HOURS);
    }

    @Test
    @DisplayName("Should delete key successfully")
    void testDelete_Success() {
        redisCacheService.delete(testKey);

        verify(redisTemplate, times(1)).delete(testKey);
    }

    @Test
    @DisplayName("Should check if key exists successfully")
    void testExists_Success() {
        when(redisTemplate.hasKey(testKey)).thenReturn(true);

        boolean exists = redisCacheService.exists(testKey);

        assertThat(exists).isTrue();
        verify(redisTemplate, times(1)).hasKey(testKey);
    }

    @Test
    @DisplayName("Should return false when key doesn't exist")
    void testExists_NotFound() {
        when(redisTemplate.hasKey(testKey)).thenReturn(false);

        boolean exists = redisCacheService.exists(testKey);

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should cache session successfully")
    void testCacheSession_Success() {
        redisCacheService.cacheSession(refreshToken, userId, "test@example.com", "FREELANCER");

        verify(valueOperations, times(1)).set(
                eq("session:" + refreshToken),
                any(),
                eq(7L),
                eq(TimeUnit.DAYS)
        );
    }

    @Test
    @DisplayName("Should get session successfully")
    void testGetSession_Success() {
        Map<String, Object> sessionData = new HashMap<>();
        sessionData.put("userId", userId.toString());
        sessionData.put("email", "test@example.com");
        sessionData.put("role", "FREELANCER");

        when(valueOperations.get("session:" + refreshToken)).thenReturn(sessionData);

        Map<String, Object> result = redisCacheService.getSession(refreshToken);

        assertThat(result).isNotNull();
        assertThat(result.get("userId")).isEqualTo(userId.toString());
    }

    @Test
    @DisplayName("Should delete session successfully")
    void testDeleteSession_Success() {
        redisCacheService.deleteSession(refreshToken);

        verify(redisTemplate, times(1)).delete("session:" + refreshToken);
    }

    @Test
    @DisplayName("Should cache profile successfully")
    void testCacheProfile_Success() {
        Map<String, Object> profileData = new HashMap<>();
        profileData.put("name", "Test User");
        profileData.put("email", "test@example.com");

        redisCacheService.cacheProfile(userId, profileData);

        verify(valueOperations, times(1)).set(
                eq("profile:" + userId),
                any(),
                eq(1L),
                eq(TimeUnit.HOURS)
        );
    }

    @Test
    @DisplayName("Should get cached profile successfully")
    @SuppressWarnings("unchecked")
    void testGetCachedProfile_Success() {
        Map<String, Object> profileData = new HashMap<>();
        profileData.put("name", "Test User");
        profileData.put("email", "test@example.com");

        when(valueOperations.get("profile:" + userId)).thenReturn(profileData);

        Map<String, Object> result = redisCacheService.getCachedProfile(userId, Map.class);

        assertThat(result).isNotNull();
        assertThat(result.get("name")).isEqualTo("Test User");
    }

    @Test
    @DisplayName("Should invalidate profile successfully")
    void testInvalidateProfile_Success() {
        redisCacheService.invalidateProfile(userId);

        verify(redisTemplate, times(1)).delete("profile:" + userId);
    }

    @Test
    @DisplayName("Should cache project list successfully")
    void testCacheProjectList_Success() {
        String filtersHash = "abc123";
        var projectIds = Set.of("proj1", "proj2", "proj3");

        redisCacheService.cacheProjectList(filtersHash, projectIds);

        verify(valueOperations, times(1)).set(
                eq("projects:list:" + filtersHash),
                eq(projectIds),
                eq(15L),
                eq(TimeUnit.MINUTES)
        );
    }

    @Test
    @DisplayName("Should get cached project list successfully")
    void testGetCachedProjectList_Success() {
        String filtersHash = "abc123";
        var projectIds = Set.of("proj1", "proj2", "proj3");

        when(valueOperations.get("projects:list:" + filtersHash)).thenReturn(projectIds);

        var result = redisCacheService.getCachedProjectList(filtersHash, Set.class);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
    }

    @Test
    @DisplayName("Should invalidate all project listings successfully")
    void testInvalidateProjectListings_Success() {
        Set<String> keys = Set.of("projects:list:abc123", "projects:list:def456");
        when(redisTemplate.keys("projects:list:*")).thenReturn(keys);

        redisCacheService.invalidateProjectListings();

        verify(redisTemplate, times(1)).delete(keys);
    }

    @Test
    @DisplayName("Should add user to online set successfully")
    void testAddOnlineUser_Success() {
        redisCacheService.addOnlineUser(userId);

        verify(setOperations, times(1)).add("online:users", userId.toString());
    }

    @Test
    @DisplayName("Should remove user from online set successfully")
    void testRemoveOnlineUser_Success() {
        redisCacheService.removeOnlineUser(userId);

        verify(setOperations, times(1)).remove("online:users", userId.toString());
    }

    @Test
    @DisplayName("Should get all online users successfully")
    void testGetOnlineUsers_Success() {
        Set<Object> onlineUsers = Set.of(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                userId.toString()
        );

        when(setOperations.members("online:users")).thenReturn(onlineUsers);

        Set<UUID> result = redisCacheService.getOnlineUsers();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
    }

    @Test
    @DisplayName("Should check if user is online successfully")
    void testIsUserOnline_Success() {
        when(setOperations.isMember("online:users", userId.toString())).thenReturn(true);

        boolean isOnline = redisCacheService.isUserOnline(userId);

        assertThat(isOnline).isTrue();
    }

    @Test
    @DisplayName("Should get online users count successfully")
    void testGetOnlineUsersCount_Success() {
        when(setOperations.size("online:users")).thenReturn(5L);

        long count = redisCacheService.getOnlineUsersCount();

        assertThat(count).isEqualTo(5);
    }

    @Test
    @DisplayName("Should increment counter successfully")
    void testIncrement_Success() {
        when(valueOperations.increment(testKey)).thenReturn(1L);

        Long result = redisCacheService.increment(testKey);

        assertThat(result).isEqualTo(1);
        verify(valueOperations, times(1)).increment(testKey);
    }

    @Test
    @DisplayName("Should increment counter by delta successfully")
    void testIncrement_WithDelta() {
        when(valueOperations.increment(testKey, 5)).thenReturn(5L);

        Long result = redisCacheService.increment(testKey, 5);

        assertThat(result).isEqualTo(5);
        verify(valueOperations, times(1)).increment(testKey, 5);
    }

    @Test
    @DisplayName("Should get counter value successfully")
    void testGetCounter_Success() {
        when(valueOperations.get(testKey)).thenReturn(42L);

        Long result = redisCacheService.getCounter(testKey);

        assertThat(result).isEqualTo(42);
    }

    @Test
    @DisplayName("Should set TTL for existing key successfully")
    void testExpire_Success() {
        redisCacheService.expire(testKey, 30, TimeUnit.MINUTES);

        verify(redisTemplate, times(1)).expire(testKey, 30, TimeUnit.MINUTES);
    }

    @Test
    @DisplayName("Should get keys by pattern successfully")
    void testKeys_Success() {
        Set<String> expectedKeys = Set.of("test:key1", "test:key2", "test:key3");
        when(redisTemplate.keys("test:*")).thenReturn(expectedKeys);

        Set<String> result = redisCacheService.keys("test:*");

        assertThat(result).isNotNull();
        assertThat(result).containsExactlyInAnyOrderElementsOf(expectedKeys);
    }

    @Test
    @DisplayName("Should delete multiple keys successfully")
    void testDeleteMultiple_Success() {
        Set<String> keys = Set.of("key1", "key2", "key3");

        redisCacheService.delete(keys);

        verify(redisTemplate, times(1)).delete(keys);
    }

    @Test
    @DisplayName("Should return empty set when no online users")
    void testGetOnlineUsers_Empty() {
        when(setOperations.members("online:users")).thenReturn(null);

        Set<UUID> result = redisCacheService.getOnlineUsers();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return empty set when online users is empty")
    void testGetOnlineUsers_EmptySet() {
        when(setOperations.members("online:users")).thenReturn(new HashSet<>());

        Set<UUID> result = redisCacheService.getOnlineUsers();

        assertThat(result).isEmpty();
    }
}
