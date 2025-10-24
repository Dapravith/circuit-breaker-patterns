package com.circuitbreaker.circuit_breaker_patterns.service.impl;


import com.circuitbreaker.circuit_breaker_patterns.enums.*;
import com.circuitbreaker.circuit_breaker_patterns.service.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.stereotype.*;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final CircuitBreakerService circuitBreakerService;
    private final Random random = new Random();

    public Map<String, Object> sendEmail(Map<String, Object> emailData) {
        return circuitBreakerService.executeWithCircuitBreaker(
                ServiceType.NOTIFICATION_SERVICE,
                () -> {
                    simulateEmailSending();
                    return Map.of(
                            "messageId", "msg_" + System.currentTimeMillis(),
                            "to", emailData.get("to"),
                            "subject", emailData.get("subject"),
                            "status", "sent",
                            "service", "notification-service"
                    );
                },
                () -> {
                    log.info("Fallback executed for email service");
                    return Map.of(
                            "messageId", "fallback_msg_" + System.currentTimeMillis(),
                            "to", emailData.get("to"),
                            "subject", emailData.get("subject"),
                            "status", "queued",
                            "service", "notification-service-fallback",
                            "fallback", true,
                            "message", "Email will be sent later when service is available"
                    );
                }
        );
    }

    public Map<String, Object> sendSms(Map<String, Object> smsData) {
        return circuitBreakerService.executeWithCircuitBreaker(
                ServiceType.NOTIFICATION_SERVICE,
                () -> {
                    simulateSmsSending();
                    return Map.of(
                            "messageId", "sms_" + System.currentTimeMillis(),
                            "to", smsData.get("to"),
                            "status", "sent",
                            "service", "notification-service"
                    );
                },
                () -> {
                    log.info("Fallback executed for SMS service");
                    return Map.of(
                            "messageId", "fallback_sms_" + System.currentTimeMillis(),
                            "to", smsData.get("to"),
                            "status", "queued",
                            "service", "notification-service-fallback",
                            "fallback", true,
                            "message", "SMS will be sent later when service is available"
                    );
                }
        );
    }

    private void simulateEmailSending() {
        try {
            Thread.sleep(150 + random.nextInt(250));

            if (random.nextDouble () < 0.25) {
                throw new RuntimeException("Email service unavailable");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Email sending interrupted", e);
        }
    }

    private void simulateSmsSending() {
        try {
            Thread.sleep(100 + random.nextInt(200));

            if (random.nextDouble () < 0.3) {
                throw new RuntimeException("SMS service unavailable");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("SMS sending interrupted", e);
        }
    }
}
