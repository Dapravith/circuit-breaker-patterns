package com.circuitbreaker.circuit_breaker_patterns.service.impl;


import com.circuitbreaker.circuit_breaker_patterns.enums.*;
import com.circuitbreaker.circuit_breaker_patterns.service.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.stereotype.*;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final CircuitBreakerService circuitBreakerService;
    private final Random random = new Random();

    public Map<String, Object> processPayment(Map<String, Object> paymentData) {
        return circuitBreakerService.executeWithCircuitBreaker(
                ServiceType.PAYMENT_SERVICE,
                () -> {
                    simulatePaymentProcessing();
                    return Map.of(
                            "transactionId", "txn_" + System.currentTimeMillis(),
                            "amount", paymentData.get("amount"),
                            "currency", paymentData.getOrDefault("currency", "USD"),
                            "status", "completed",
                            "service", "payment-service"
                    );
                },
                () -> {
                    log.info("Fallback executed for payment service");
                    return Map.of(
                            "transactionId", "fallback_txn_" + System.currentTimeMillis(),
                            "amount", paymentData.get("amount"),
                            "currency", paymentData.getOrDefault("currency", "USD"),
                            "status", "pending",
                            "service", "payment-service-fallback",
                            "fallback", true,
                            "message", "Payment will be processed later"
                    );
                }
        );
    }

    public Map<String, Object> getPaymentStatus(String transactionId) {
    return circuitBreakerService.executeWithCircuitBreaker(
            ServiceType.PAYMENT_SERVICE,
            () -> {
                simulateExternalServiceCall ();
                return Map.of (
                        "transactionId", transactionId,
                        "status", random.nextBoolean () ? "completed" : "pending",
                        "service", "payment-service"
                );
            },
        () -> {
            log.info("Fallback executed for payment status check");
            return Map.of(
                    "transactionId", transactionId,
                    "status", "unknown",
                    "service", "payment-service-fallback",
                    "fallback", true,
                    "message", "Unable to retrieve payment status at this time"
            );
        }
    );
    }

    private void simulatePaymentProcessing() {
        try {
            Thread.sleep(100 + random.nextInt(200));

            if (random.nextDouble() < 0.4) {
                throw new RuntimeException("Simulated payment processing failure");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread interrupted", e);
        }
    }

    private void simulateExternalServiceCall() {
        try {
            Thread.sleep(50 + random.nextInt(100));

            if (random.nextDouble() < 0.3) {
                throw new RuntimeException("Simulated external service failure");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread interrupted", e);
        }
    }
}
