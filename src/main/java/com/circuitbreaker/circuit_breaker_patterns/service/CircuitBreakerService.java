package com.circuitbreaker.circuit_breaker_patterns.service;

import com.circuitbreaker.circuit_breaker_patterns.enums.*;

import java.util.function.*;

public interface CircuitBreakerService {

    <T> T executeWithCircuitBreaker(ServiceType serviceType, Supplier<T> operation);

    <T> T executeWithCircuitBreaker(ServiceType serviceType, Supplier<T> operation, Supplier<T> fallback);

    void resetCircuitBreaker(ServiceType serviceType);

    String getCircuitBreakerState(ServiceType serviceType);
}
