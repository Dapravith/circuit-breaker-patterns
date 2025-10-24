package com.circuitbreaker.circuit_breaker_patterns.controller;


import com.circuitbreaker.circuit_breaker_patterns.service.impl.*;
import lombok.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("")
    public ResponseEntity<Map<String, Object>> processPayment(@RequestBody Map<String, Object> paymentData) {
        return ResponseEntity.ok(paymentService.processPayment(paymentData));
    }

    @GetMapping("/{transactionId}/status")
    public ResponseEntity<Map<String, Object>> getPaymentStatus(@PathVariable String transactionId) {
        return ResponseEntity.ok (paymentService.getPaymentStatus (transactionId));
    }
}
