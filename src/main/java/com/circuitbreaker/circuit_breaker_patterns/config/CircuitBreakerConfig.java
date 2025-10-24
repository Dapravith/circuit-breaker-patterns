package com.circuitbreaker.circuit_breaker_patterns.config;

import io.github.resilience4j.circuitbreaker.*;
import org.springframework.context.annotation.*;

@Configuration
public class CircuitBreakerConfig {

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        return CircuitBreakerRegistry.ofDefaults();
    }
}
