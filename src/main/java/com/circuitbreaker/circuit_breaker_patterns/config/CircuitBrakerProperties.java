package com.circuitbreaker.circuit_breaker_patterns.config;

import lombok.*;
import org.springframework.boot.context.properties.*;
import org.springframework.stereotype.*;

import java.time.*;
import java.util.*;

@Data
@Component
@ConfigurationProperties(prefix = "circuit-breaker")
public class CircuitBrakerProperties {

    private Map<String, ServiceConfig> services = new java.util.HashMap<>();

    @Data
    public static class ServiceConfig {
        private int failureRateThreshold = 50;
        private java.time.Duration waitDurationInOpenState = java.time.Duration.ofSeconds(60);
        private int slidingWindowSize = 10;
        private int minimumNumberOfCalls = 5;
        private java.time.Duration timeoutDuration = java.time.Duration.ofSeconds(2);
        private boolean automaticTransitionFromOpenToHalfOpenEnabled = true;
        private int permittedNumberOfCallsInHalfOpenState = 2;

        public int getFailureRateThreshold() { return failureRateThreshold; }
        public java.time.Duration getWaitDurationInOpenState() { return waitDurationInOpenState; }
        public int getSlidingWindowSize() { return slidingWindowSize; }
        public int getMinimumNumberOfCalls() { return minimumNumberOfCalls; }
        public java.time.Duration getTimeoutDuration() { return timeoutDuration; }
        public boolean isAutomaticTransitionFromOpenToHalfOpenEnabled() { return automaticTransitionFromOpenToHalfOpenEnabled; }
        public int getPermittedNumberOfCallsInHalfOpenState() { return permittedNumberOfCallsInHalfOpenState; }
    }

}
