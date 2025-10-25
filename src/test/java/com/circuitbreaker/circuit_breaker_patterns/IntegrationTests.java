package com.circuitbreaker.circuit_breaker_patterns;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;
import org.springframework.boot.test.web.client.*;
import org.springframework.http.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IntegrationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void userCreateAndGet() {
        Map<String, Object> newUser = Map.of(
                "name", "Integration Tester",
                "email", "inttester@example.com"
        );

        ResponseEntity<Map> postResp = restTemplate.postForEntity("/api/users", newUser, Map.class);
        assertThat(postResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map body = postResp.getBody();
        assertThat(body).isNotNull();
        assertThat(body).containsKey("id");

        String id = String.valueOf(body.get("id"));
        ResponseEntity<Map> getResp = restTemplate.getForEntity("/api/users/" + id, Map.class);
        assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map getBody = getResp.getBody();
        assertThat(getBody).isNotNull();
        assertThat(getBody.get("email")).isEqualTo("inttester@example.com");
    }

    @Test
    void paymentProcess() {
        Map<String, Object> payment = Map.of(
                "amount", 49.99,
                "currency", "USD"
        );
        ResponseEntity<Map> resp = restTemplate.postForEntity("/api/payments", payment, Map.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map body = resp.getBody();
        assertThat(body).isNotNull();
        assertThat(body).containsKey("transactionId");
        assertThat(body).containsKey("status");
    }

    @Test
    void notificationEmail() {
        Map<String, Object> email = Map.of(
                "to", "user@example.com",
                "subject", "Test",
                "body", "Hello"
        );
        ResponseEntity<Map> resp = restTemplate.postForEntity("/api/notifications/email", email, Map.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map body = resp.getBody();
        assertThat(body).isNotNull();
        assertThat(body).containsKey("messageId");
        assertThat(body).containsKey("status");
    }
}

