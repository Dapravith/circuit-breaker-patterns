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

## Demo UI
A minimal full-stack demo UI is included so you can exercise the REST APIs from your browser.

- File: `src/main/resources/static/index.html`
- After starting the application (see "Build & Run"), open: `http://localhost:8080/`
- The UI includes controls to create/get users, process payments, send email notifications, and inspect/reset circuit breakers.

## Postman Collection
A Postman collection is included for easy API exploration and manual testing.

- File: `postman/CircuitBreakerPatterns.postman_collection.json`
- Import this JSON in Postman and set the `baseUrl` variable to `http://localhost:8080` (or your server URL).
- The collection contains requests for creating users, processing payments, sending notifications, and managing circuit breakers.

## Quick Verification (what I changed & test results)
I implemented the following to make the project a runnable, testable full-stack demo:
- Added RESTful user CRUD endpoints (GET `/api/users/{userId}`, POST `/api/users`).
- Kept external demo endpoints under `/api/users/external/{userId}` which call a remote API (with a circuit breaker).
- Added an in-memory user store in `UserService` for demo CRUD operations.
- Ensured `RestTemplate` bean is configured and used for external calls.
- Added a minimal static UI at `src/main/resources/static/index.html` to exercise the APIs from a browser.
- Added a Postman collection at `postman/CircuitBreakerPatterns.postman_collection.json`.
- Added comprehensive integration tests in `src/test/java/.../IntegrationTests.java` that exercise user creation, payment processing, and notification sending.

Test run summary (on my machine):
- `./mvnw test` — SUCCESS (4 tests passed: integration and context-load).

## How to run locally
Start the application:

```bash
./mvnw clean package
./mvnw spring-boot:run
```

Run tests:

```bash
./mvnw test
```

Open the demo UI:

- Visit: `http://localhost:8080/` and use the UI to call the APIs.

Use Postman:

- Import `postman/CircuitBreakerPatterns.postman_collection.json` and run the requests; make sure `baseUrl` is set to your server URL.

Curl examples (already included in the README) — two quick examples:

```bash
# Create a user
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name": "John Doe", "email": "john@example.com"}'

# Check circuit breaker state for USER_SERVICE
curl http://localhost:8080/api/circuit-breaker/USER_SERVICE/state
```

## Next steps and suggestions
- Add a Dockerfile and docker-compose to run the app and a mock external API for deterministic testing.
- Add end-to-end (E2E) tests that run the UI against the running service.
- Replace `RestTemplate` with `WebClient` for non-blocking calls if you plan to scale.
- Add authentication and request validation for production readiness.

## Autopush (automated local push) scripts
Two small helper scripts are included to automate the typical edit → test → commit → push cycle. They are intended for developer convenience (local use) and are not enabled by default in CI.

Files:
- `scripts/autopush.sh` — Safe, single-run script that runs tests, stages all changes, commits, and pushes to the current branch. It aborts if tests fail.
- `scripts/autopush-watch.sh` — Optional file-watcher that invokes `autopush.sh` whenever project files change. On macOS it uses `fswatch` (recommended); otherwise it falls back to a polling implementation.

Important safety notes:
- `autopush.sh` stages all working-tree changes (`git add -A`) before committing. Review your changes or modify the script to be more selective if you have sensitive files or untracked content you don't want pushed.
- The scripts assume your git credentials are set up (SSH key or cached HTTPS credentials). They do not handle interactive credential prompts.
- The watcher runs commands automatically — only enable it on trusted branches and environments. Prefer using it on local feature branches.
- The scripts run `./mvnw test` and will abort the push if tests fail. This helps prevent broken code from being pushed accidentally.

Usage examples

Run a one-off autopush:

```bash
# Use an explicit commit message
./scripts/autopush.sh "WIP: add feature X"

# Use auto-generated timestamped commit message
./scripts/autopush.sh
```

Run the file watcher (macOS recommended):

```bash
# Watch the repo root and debounce 2 seconds (default)
./scripts/autopush-watch.sh

# Watch a specific folder and use a 5-second debounce
./scripts/autopush-watch.sh src/main/java 5
```

Customizing behavior
- To skip tests (not recommended), edit `scripts/autopush.sh` and change the test command. Or add a `--skip-tests` flag implementation.
- To limit what is committed, replace `git add -A` with a more selective `git add <paths>` step.

Troubleshooting
- If `./scripts/autopush.sh` fails with a git error, ensure your working tree is in a consistent state (resolve conflicts) and your credentials are set up.
- If the watcher does not detect changes on macOS, install `fswatch` with Homebrew: `brew install fswatch`.

Security reminder
- Never embed secrets or private keys into commits. Use environment variables and `.gitignore` for local-only files.

## License
MIT License

## Credits
- [Resilience4j](https://resilience4j.io/)
- [Spring Boot](https://spring.io/projects/spring-boot)
- [Lombok](https://projectlombok.org/)

---
For questions or issues, open an issue in this repository.
