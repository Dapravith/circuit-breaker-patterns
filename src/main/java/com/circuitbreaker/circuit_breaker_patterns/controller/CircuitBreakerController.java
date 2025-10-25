package com.circuitbreaker.circuit_breaker_patterns.controller;

import com.circuitbreaker.circuit_breaker_patterns.enums.*;
import com.circuitbreaker.circuit_breaker_patterns.service.*;
import lombok.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/circuit-breaker")
public class CircuitBreakerController {

    private final CircuitBreakerService circuitBreakerService;

    @GetMapping("/{serviceType}/state")
    public ResponseEntity<Map<String, String>> getCircuitBreakerState(@PathVariable String serviceType) {
        try {
            ServiceType type = ServiceType.valueOf(serviceType.toUpperCase());
            String state = circuitBreakerService.getCircuitBreakerState(type);
            return ResponseEntity.ok(Map.of(
                    "serviceType", type.getServiceName(),
                    "state", state
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", "Invalid service type: " + serviceType
            ));
        }
    }

    @PostMapping("/{serviceType}/reset")
    public ResponseEntity<Map<String, String>> resetCircuitBreaker(@PathVariable String serviceType) {
        try {
            ServiceType type = ServiceType.valueOf(serviceType.toUpperCase());
            circuitBreakerService.resetCircuitBreaker(type);
            return ResponseEntity.ok(Map.of("serviceType", type.getServiceName(), "status", "reset"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", "Invalid service type: " + serviceType
            ));
        }
    }

    @PostMapping("/{serviceType}/force/{state}")
    public ResponseEntity<Map<String, String>> forceCircuitBreakerState(@PathVariable String serviceType, @PathVariable String state) {
        try {
            ServiceType type = ServiceType.valueOf(serviceType.toUpperCase());

            // Validate state
            String normalized = state.toUpperCase().replace('-', '_');
            if (!Set.of("OPEN", "CLOSED", "HALF_OPEN").contains(normalized)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                        "error", "Invalid state value. Allowed: OPEN, HALF_OPEN, CLOSED"
                ));
            }

            circuitBreakerService.forceCircuitBreakerState(type, normalized);
            return ResponseEntity.ok(Map.of("serviceType", type.getServiceName(), "status", normalized));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", "Invalid service type or state: " + serviceType + " / " + state
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "Failed to force state: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/states")
    public ResponseEntity<Map<String, String>> getAllCircuitBreakerStates() {
        try {
            Map<String, String> states = Map.of(
                    "USER_SERVICE", circuitBreakerService.getCircuitBreakerState(ServiceType.USER_SERVICE),
                    "PAYMENT_SERVICE", circuitBreakerService.getCircuitBreakerState(ServiceType.PAYMENT_SERVICE),
                    "NOTIFICATION_SERVICE", circuitBreakerService.getCircuitBreakerState(ServiceType.NOTIFICATION_SERVICE)
            );
            return ResponseEntity.ok(states);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "Failed to retrieve circuit breaker states" + e.getMessage()
            ));
        }
    }

    @GetMapping("/states")
    public ResponseEntity<Map<String, String>> getAllCircuitBreakerStatesGet() {
        return getAllCircuitBreakerStates();
    }
}
