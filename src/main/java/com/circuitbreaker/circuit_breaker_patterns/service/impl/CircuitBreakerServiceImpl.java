package com.circuitbreaker.circuit_breaker_patterns.service.impl;


import com.circuitbreaker.circuit_breaker_patterns.config.CircuitBrakerProperties;
import com.circuitbreaker.circuit_breaker_patterns.enums.*;
import com.circuitbreaker.circuit_breaker_patterns.service.*;
import io.github.resilience4j.circuitbreaker.*;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.springboot3.circuitbreaker.autoconfigure.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.cloud.client.circuitbreaker.*;
import org.springframework.stereotype.*;

import java.util.concurrent.*;
import java.util.function.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CircuitBreakerServiceImpl implements CircuitBreakerService {

    private final CircuitBrakerProperties circuitBrakerProperties;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final ConcurrentHashMap<ServiceType, CircuitBreaker> circuitBreakerCache = new ConcurrentHashMap<>();

    @Override
    public <T> T executeWithCircuitBreaker(ServiceType serviceType, Supplier<T> operation) {
        CircuitBreaker circuitBreaker = getOrCreateCircuitBreaker(serviceType);
        return circuitBreaker.executeSupplier(operation);
    }

    @Override
    public <T> T executeWithCircuitBreaker(ServiceType serviceType, Supplier<T> operation, Supplier<T> fallback) {
        CircuitBreaker circuitBreaker = getOrCreateCircuitBreaker(serviceType);
        try {
            return circuitBreaker.executeSupplier(operation);
        } catch (Exception e) {
            log.warn("Circuit breaker fallback triggered for service: {}", serviceType.getServiceName(), e);
            return fallback.get();
        }
    }

    @Override
    public void resetCircuitBreaker(ServiceType serviceType) {
        CircuitBreaker circuitBreaker = circuitBreakerCache.get(serviceType);
        if (circuitBreaker != null) {
            circuitBreaker.reset();
            log.info("Circuit breaker reset for service: {}", serviceType.getServiceName());
        }
    }

    @Override
    public String getCircuitBreakerState(ServiceType serviceType) {
        CircuitBreaker circuitBreaker = getOrCreateCircuitBreaker(serviceType);
        return circuitBreaker.getState().toString();
    }

    private CircuitBreaker getOrCreateCircuitBreaker(ServiceType serviceType) {
        return circuitBreakerCache.computeIfAbsent(serviceType, this::createCircuitBreaker);
    }

    private CircuitBreaker createCircuitBreaker(ServiceType serviceType) {
        String serviceName = serviceType.getServiceName();
        CircuitBrakerProperties.ServiceConfig config = circuitBrakerProperties.getServices()
                .getOrDefault(serviceName, new CircuitBrakerProperties.ServiceConfig());

        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(config.getFailureRateThreshold())
                .waitDurationInOpenState(config.getWaitDurationInOpenState())
                .slidingWindowSize(config.getSlidingWindowSize())
                .minimumNumberOfCalls(config.getMinimumNumberOfCalls())
                .automaticTransitionFromOpenToHalfOpenEnabled(config.isAutomaticTransitionFromOpenToHalfOpenEnabled())
                .permittedNumberOfCallsInHalfOpenState(config.getPermittedNumberOfCallsInHalfOpenState())
                .build();

        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(serviceName, circuitBreakerConfig);

        circuitBreaker.getEventPublisher()
                .onStateTransition(event -> log.info("Circuit breaker state changed for {}: {} -> {}",
                        serviceName, event.getStateTransition().getFromState(), event.getStateTransition().getToState()));

        log.info("Created circuit breaker for service: {} with config: {}", serviceName, config);
        return circuitBreaker;
    }
}
