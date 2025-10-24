# Circuit Breaker Patterns - Spring Boot Example

## Overview
This project demonstrates the Circuit Breaker pattern in a Spring Boot microservice architecture using [Resilience4j](https://resilience4j.io/). It provides REST APIs for user, payment, and notification services, each protected by a configurable circuit breaker to improve fault tolerance and resilience.

## Motivation
Microservices often depend on external systems or other services. If a dependency fails or becomes slow, it can cascade failures throughout your system. The Circuit Breaker pattern helps prevent this by monitoring calls and temporarily blocking requests to failing services, allowing them time to recover.

## Features
- Circuit breaker for each service (User, Payment, Notification)
- Configurable thresholds and timeouts via `application.yml`
- Automatic fallback logic for failed requests
- REST APIs to query and reset circuit breaker states
- Centralized exception handling
- Integration with external APIs using RestTemplate

## Architecture
- **Spring Boot** for rapid development
- **Resilience4j** for circuit breaker implementation
- **Lombok** for boilerplate reduction
- **RestTemplate** for HTTP calls
- **Custom configuration** via `CircuitBrakerProperties`

## Circuit Breaker Pattern
A circuit breaker monitors calls to external services:
- **Closed:** Calls flow normally. If failures exceed a threshold, the breaker opens.
- **Open:** Calls are blocked and fail-fast. After a wait period, the breaker transitions to half-open.
- **Half-Open:** Allows limited test calls. If successful, closes; if failures persist, reopens.

## Getting Started
### Prerequisites
- Java 21+
- Maven

### Build & Run
```sh
./mvnw clean package
./mvnw spring-boot:run
```
The app runs on `http://localhost:8080`.

### Configuration
Edit `src/main/resources/application.yml` to set circuit breaker thresholds, timeouts, and service configs:
```yaml
circuit-breaker:
  services:
    USER_SERVICE:
      failureRateThreshold: 50
      waitDurationInOpenState: 60s
      slidingWindowSize: 10
      minimumNumberOfCalls: 5
      timeoutDuration: 2s
      automaticTransitionFromOpenToHalfOpenEnabled: true
      permittedNumberOfCallsInHalfOpenState: 2
```

## API Endpoints

### User Service
- `GET /api/users/{userId}` - Get user by ID
- `POST /api/users` - Create new user

### Payment Service
- `POST /api/payments` - Process payment
- `GET /api/payments/{transactionId}/status` - Get payment status

### Notification Service
- `POST /api/notifications/email` - Send email
- `POST /api/notifications/sms` - Send SMS

### Circuit Breaker Management
- `GET /api/circuit-breaker/{serviceType}/state` - Get circuit breaker state
- `POST /api/circuit-breaker/{serviceType}/reset` - Reset circuit breaker
- `GET /api/circuit-breaker/states` - Get all circuit breaker states

### Monitoring
- `GET /actuator/health` - Application health
- `GET /actuator/circuitbreakers` - Circuit breaker metrics

## Configuration

Circuit breaker properties are configured in `application.yml`:

```yaml
circuit-breaker:
  services:
    user-service:
      failure-rate-threshold: 50
      wait-duration-in-open-state: 60s
      sliding-window-size: 10
      minimum-number-of-calls: 5
      timeout-duration: 3s
      automatic-transition-from-open-to-half-open-enabled: true
      permitted-number-of-calls-in-half-open-state: 3
```

## Circuit Breaker Configuration Parameters
- **failure-rate-threshold:** Percentage of failed calls that triggers the circuit to open (0-100)
- **wait-duration-in-open-state:** How long the circuit stays OPEN before transitioning to HALF_OPEN (e.g., 60s, 2m)
- **sliding-window-size:** Number of recent calls used to calculate the failure rate
- **minimum-number-of-calls:** Minimum calls required before the circuit can open
- **timeout-duration:** Maximum time allowed for each individual call (e.g., 3s, 500ms)
- **automatic-transition-from-open-to-half-open-enabled:** Automatically transition from OPEN to HALF_OPEN after wait period (true/false)
- **permitted-number-of-calls-in-half-open-state:** Number of test calls allowed when circuit is HALF_OPEN

## Circuit Breaker States
CLOSED → OPEN → HALF_OPEN → CLOSED
   ↑         ↓         ↓
   └─────────┴─────────┘
- **CLOSED:** Normal operation, all calls go through to the service
- **OPEN:** Circuit is tripped, calls are rejected immediately with fallback
- **HALF_OPEN:** Testing if service is recovered (limited calls allowed)

### State Transition Example
- Service starts failing → After 5+ calls with 50%+ failure rate → Circuit OPENS
- Circuit is OPEN → For 60 seconds → All requests return fallback responses
- Wait period ends → Circuit goes to HALF_OPEN → 3 test calls are made
- Test results:
  - If successful → Circuit CLOSES (back to normal)
  - If failed → Circuit reopens for another 60 seconds

## Usage Examples

### Creating a User
```sh
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name": "John Doe", "email": "john@example.com"}'
```

### Processing a Payment
```sh
curl -X POST http://localhost:8080/api/payments \
  -H "Content-Type: application/json" \
  -d '{"amount": 100.00, "currency": "USD"}'
```

### Checking Circuit Breaker State
```sh
curl http://localhost:8080/api/circuit-breaker/USER_SERVICE/state
```

### Resetting Circuit Breaker
```sh
curl -X POST http://localhost:8080/api/circuit-breaker/USER_SERVICE/reset
```

## Fallback Logic
If an external service fails or times out, a fallback response is returned, indicating the error and providing default data.

## Exception Handling
All exceptions are handled by `GlobalExceptionHandler`, returning meaningful error messages and HTTP status codes.

## Testing
Run all tests:
```sh
./mvnw test
```

## Troubleshooting
- **500 Internal Server Error:** Check logs for stack traces. Common causes: missing beans, misconfigured circuit breaker, or external API issues.
- **StackOverflowError:** Ensure no recursive method calls in service logic.
- **No qualifying bean of type 'RestTemplate':** Make sure `RestTemplateConfig` provides a `RestTemplate` bean.

## Extending
- Add new services by updating `ServiceType` enum and configuration.
- Customize circuit breaker settings per service in `application.yml`.

## License
MIT License

## Credits
- [Resilience4j](https://resilience4j.io/)
- [Spring Boot](https://spring.io/projects/spring-boot)
- [Lombok](https://projectlombok.org/)

---
For questions or issues, open an issue in this repository.
