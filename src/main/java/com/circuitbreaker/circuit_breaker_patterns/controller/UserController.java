package com.circuitbreaker.circuit_breaker_patterns.controller;


import com.circuitbreaker.circuit_breaker_patterns.service.*;
import lombok.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("api/users/external/{userId}")
    public ResponseEntity<Map<String, Object>> getUSerWithRestTemplate(@PathVariable String userId) {
        return ResponseEntity.ok(userService.getUserByIdWithRestTemplate(userId));
    }

    @PutMapping("api/users/external/{userId}")
    public ResponseEntity<Map<String, Object>> updateUserWithRestTemplate(@PathVariable String userId) {
        return ResponseEntity.ok (userService.getUserByIdWithRestTemplate (userId));
    }

    @DeleteMapping("api/users/external/{userId}")
    public ResponseEntity<Map<String, Object>> deleteUserWithRestTemplate(@PathVariable String userId) {
        return ResponseEntity.ok (userService.getUserByIdWithRestTemplate (userId));
    }
}
