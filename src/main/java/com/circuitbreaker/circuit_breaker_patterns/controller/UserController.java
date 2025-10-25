package com.circuitbreaker.circuit_breaker_patterns.controller;


import com.circuitbreaker.circuit_breaker_patterns.service.*;
import lombok.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    // Public CRUD endpoints
    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getUser(@PathVariable String userId) {
        Map<String, Object> user = userService.getUserById(userId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
        }
        return ResponseEntity.ok(user);
    }

    @PostMapping("")
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody Map<String, Object> userData) {
        Map<String, Object> created = userService.createUser(userData);
        return ResponseEntity.ok(created);
    }

    // Keep external endpoints (call external API via RestTemplate)
    @GetMapping("/external/{userId}")
    public ResponseEntity<Map<String, Object>> getUserExternal(@PathVariable String userId) {
        return ResponseEntity.ok(userService.getUserByIdWithRestTemplate(userId));
    }

    @PutMapping("/external/{userId}")
    public ResponseEntity<Map<String, Object>> updateUserExternal(@PathVariable String userId) {
        return ResponseEntity.ok(userService.getUserByIdWithRestTemplate(userId));
    }

    @DeleteMapping("/external/{userId}")
    public ResponseEntity<Map<String, Object>> deleteUserExternal(@PathVariable String userId) {
        return ResponseEntity.ok(userService.getUserByIdWithRestTemplate(userId));
    }
}
