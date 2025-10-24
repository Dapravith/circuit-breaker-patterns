package com.circuitbreaker.circuit_breaker_patterns.service;


import com.circuitbreaker.circuit_breaker_patterns.enums.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.stereotype.*;
import org.springframework.web.client.*;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final CircuitBreakerService circuitBreakerService;
    private final RestTemplate restTemplate;

    public Map<String, Object> getUserByIdWithRestTemplate(String userId) {
        return circuitBreakerService.executeWithCircuitBreaker (
                ServiceType.USER_SERVICE,
                () -> getUserByIdWithRestTemplateOperation(userId),
                () -> getUserByIdCallback(userId)
        );
    }

    private Map<String, Object> getUserByIdWithRestTemplateOperation(String userId) {
        try {
            // Call external API with timeout (configured in RestTemplateConfig)
            String url = "https://jsonplaceholder.typicode.com/users/" + userId;
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

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
        } catch (org.springframework.web.client.ResourceAccessException e) {
            log.error("Network/Timeout error for user: {}", userId, e);
            throw new RuntimeException("Network error or timeout: " + e.getMessage(), e);
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            log.error("Server error (5xx) for user: {}", userId, e);
            throw new RuntimeException("Internal Server Error: " + e.getResponseBodyAsString(), e);
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("Client error (4xx) for user: {}", userId, e);
            throw new RuntimeException("Client Error: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("RestTemplate call failed for user: {}", userId, e);
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
