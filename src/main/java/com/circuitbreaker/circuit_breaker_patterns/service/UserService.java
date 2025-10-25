package com.circuitbreaker.circuit_breaker_patterns.service;


import com.circuitbreaker.circuit_breaker_patterns.enums.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.stereotype.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final CircuitBreakerService circuitBreakerService;
    private final WebClient webClient;

    // In-memory user store for demo purposes
    private final ConcurrentHashMap<String, Map<String, Object>> userStore = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(System.currentTimeMillis());

    // Public CRUD methods
    public Map<String, Object> getUserById(String userId) {
        return userStore.get(userId);
    }

    public Map<String, Object> createUser(Map<String, Object> userData) {
        String id = String.valueOf(idCounter.incrementAndGet());
        Map<String, Object> user = new HashMap<>();
        user.put("id", id);
        user.put("name", userData.getOrDefault("name", "Unknown"));
        user.put("email", userData.getOrDefault("email", "unknown@example.com"));
        user.put("createdAt", System.currentTimeMillis());
        userStore.put(id, user);
        return user;
    }

    public Map<String, Object> getUserByIdWithRestTemplate(String userId) {
        return circuitBreakerService.executeWithCircuitBreaker (
                ServiceType.USER_SERVICE,
                () -> getUserByIdWithRestTemplateOperation(userId),
                () -> getUserByIdCallback(userId)
        );
    }

    private Map<String, Object> getUserByIdWithRestTemplateOperation(String userId) {
        try {
            // Call external API with configured base-url using WebClient
            String urlPath = "/users/" + userId;
            Mono<Map> mono = webClient.get().uri(urlPath).retrieve().bodyToMono(Map.class);
            Map response = mono.block(Duration.ofSeconds(5));

            if (response != null) {
                return Map.of(
                        "id", response.getOrDefault("id", userId),
                        "name", response.getOrDefault("name", "External User"),
                        "email", response.getOrDefault("email", "external@example.com"),
                        "service", "external-api",
                        "success", true
                );
            } else {
                throw new RuntimeException("External API returned null response");
            }
        } catch (Exception e) {
            log.error("WebClient call failed for user: {}", userId, e);
            throw new RuntimeException("External service call failed: " + e.getMessage(), e);
        }
    }


    private Map<String, Object> getUserByIdCallback(String userId) {
        log.info("Fallback executed for user service - userId: {}", userId);
        return Map.of(
                "id", userId,
                "name", "Fallback User",
                "email", "default@example.com",
                "service", "user-service-fallback",
                "fallback", true,
                "error", "Service timeout or error occurred",
                "timestamp", System.currentTimeMillis(),
                "status", "fallback"
        );
    }
}
