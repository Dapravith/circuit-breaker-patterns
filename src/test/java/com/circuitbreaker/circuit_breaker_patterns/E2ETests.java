package com.circuitbreaker.circuit_breaker_patterns;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class E2ETests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void uiLoadsAndFlowsWork() {
        // Fetch UI
        ResponseEntity<String> ui = restTemplate.getForEntity("/", String.class);
        assertThat(ui.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(ui.getBody()).contains("Circuit Breaker Patterns - Demo UI");

        // Create user via API (UI would do same)
        Map<String, Object> payload = Map.of("name", "E2E User", "email", "e2e@example.com");
        ResponseEntity<Map> createResp = restTemplate.postForEntity("/api/users", payload, Map.class);
        assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(createResp.getBody()).containsKey("id");

        // Process payment
        Map<String, Object> payment = Map.of("amount", 5.0, "currency", "USD");
        ResponseEntity<Map> payResp = restTemplate.postForEntity("/api/payments", payment, Map.class);
        assertThat(payResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(payResp.getBody()).containsKey("transactionId");

        // Send notification
        Map<String, Object> email = Map.of("to", "user@example.com", "subject", "E2E", "body", "Hi");
        ResponseEntity<Map> emailResp = restTemplate.postForEntity("/api/notifications/email", email, Map.class);
        assertThat(emailResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(emailResp.getBody()).containsKey("messageId");

        // Check circuit breaker state endpoint
        ResponseEntity<Map> cbResp = restTemplate.getForEntity("/api/circuit-breaker/states", Map.class);
        assertThat(cbResp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}

