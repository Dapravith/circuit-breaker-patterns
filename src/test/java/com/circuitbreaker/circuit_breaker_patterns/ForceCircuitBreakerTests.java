package com.circuitbreaker.circuit_breaker_patterns;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ForceCircuitBreakerTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void forceAndResetCircuitBreakerStates() {
        // Ensure initial state is available (should be CLOSED by default)
        ResponseEntity<Map> initial = restTemplate.getForEntity("/api/circuit-breaker/USER_SERVICE/state", Map.class);
        assertThat(initial.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(initial.getBody()).containsKey("state");
        assertThat(initial.getBody().get("state")).isEqualTo("CLOSED");

        // Force OPEN
        ResponseEntity<Map> openResp = restTemplate.postForEntity("/api/circuit-breaker/USER_SERVICE/force/OPEN", null, Map.class);
        assertThat(openResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(openResp.getBody()).containsEntry("status", "OPEN");

        ResponseEntity<Map> stateAfterOpen = restTemplate.getForEntity("/api/circuit-breaker/USER_SERVICE/state", Map.class);
        assertThat(stateAfterOpen.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(stateAfterOpen.getBody().get("state")).isEqualTo("OPEN");

        // Force HALF_OPEN
        ResponseEntity<Map> halfResp = restTemplate.postForEntity("/api/circuit-breaker/USER_SERVICE/force/HALF_OPEN", null, Map.class);
        assertThat(halfResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(halfResp.getBody()).containsEntry("status", "HALF_OPEN");

        ResponseEntity<Map> stateAfterHalf = restTemplate.getForEntity("/api/circuit-breaker/USER_SERVICE/state", Map.class);
        assertThat(stateAfterHalf.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(stateAfterHalf.getBody().get("state")).isEqualTo("HALF_OPEN");

        // Force CLOSED
        ResponseEntity<Map> closedResp = restTemplate.postForEntity("/api/circuit-breaker/USER_SERVICE/force/CLOSED", null, Map.class);
        assertThat(closedResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(closedResp.getBody()).containsEntry("status", "CLOSED");

        ResponseEntity<Map> stateAfterClosed = restTemplate.getForEntity("/api/circuit-breaker/USER_SERVICE/state", Map.class);
        assertThat(stateAfterClosed.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(stateAfterClosed.getBody().get("state")).isEqualTo("CLOSED");
    }

    @Test
    void invalidForceStateReturnsBadRequest() {
        ResponseEntity<Map> resp = restTemplate.postForEntity("/api/circuit-breaker/USER_SERVICE/force/INVALIDSTATE", null, Map.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody()).containsKey("error");
    }
}

