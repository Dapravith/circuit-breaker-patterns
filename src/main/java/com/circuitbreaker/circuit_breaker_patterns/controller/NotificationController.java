package com.circuitbreaker.circuit_breaker_patterns.controller;


import com.circuitbreaker.circuit_breaker_patterns.service.impl.*;
import lombok.*;
import org.apache.coyote.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/email")
    public ResponseEntity<Map<String, Object>> sendEmail(@RequestBody Map<String, Object> emailData) {
        return ResponseEntity.ok (notificationService.sendEmail (emailData));
    }

    @PostMapping("/sms")
    public ResponseEntity<Map<String, Object>> sendSMS(@RequestBody Map<String, Object> smsData) {
        return ResponseEntity.ok (notificationService.sendSms (smsData));
    }
}
