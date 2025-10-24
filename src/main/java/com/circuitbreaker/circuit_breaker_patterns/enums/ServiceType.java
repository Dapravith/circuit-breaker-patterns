package com.circuitbreaker.circuit_breaker_patterns.enums;

import lombok.*;

@Getter
public enum ServiceType {

    USER_SERVICE("user-service"),
    PAYMENT_SERVICE("payment-service"),
    NOTIFICATION_SERVICE("notification-service");

    private final String serviceName;

    ServiceType(String serviceName) {
        this.serviceName = serviceName;
    }
}
