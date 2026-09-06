# Sentinel

Sentinel is a Spring Boot backend for recording, querying, filtering, and monitoring security events, built with AI-assistance.

It was built as a portfolio project to demonstrate backend API design, persistence, validation, automated testing, Docker, CI, and production-style health monitoring.

## Features

- Create, read, update, and delete security events
- Filter events by severity and event type
- Pagination and newest-first sorting
- Request validation
- Structured API error responses
- PostgreSQL persistence
- Dockerized backend and database
- Docker health checks
- Spring Boot Actuator health and info endpoints
- Integration testing with Testcontainers
- GitHub Actions CI
- Event-ingestion rate limiting with HTTP 429 responses
- Structured JSON logging
- Runtime metrics with Spring Boot Actuator
- CRITICAL-event webhook notifications

## Tech Stack

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- PostgreSQL
- Jakarta Validation
- Maven
- Testcontainers
- Docker
- Docker Compose
- GitHub Actions

## Architecture

```text
Client
  |
  v
SecurityEventController
  |
  v
SecurityEventService
  |
  v
SecurityEventRepository
  |
  v
PostgreSQL
```

The API uses DTOs to keep the external API contract separate from the persistence entity.

## Security Event Model

A security event contains:

- `id`
- `source`
- `eventType`
- `severity`
- `message`
- `ipAddress`
- `timestamp`

### Severities

- `LOW`
- `MEDIUM`
- `HIGH`
- `CRITICAL`

### Event Types

- `FAILED_LOGIN`
- `PORT_SCAN`
- `MALWARE_DETECTED`
- `SUSPICIOUS_REQUEST`

## API

### Create an event

```http
POST /api/events
```

Example:

```json
{
  "source": "auth-service",
  "eventType": "FAILED_LOGIN",
  "severity": "HIGH",
  "message": "Repeated failed login attempts",
  "ipAddress": "192.168.1.50"
}
```

### Get all events

```http
GET /api/events
```

### Get one event

```http
GET /api/events/{id}
```

### Update an event

```http
PUT /api/events/{id}
```

### Delete an event

```http
DELETE /api/events/{id}
```

### Filter events

```http
GET /api/events?severity=HIGH
GET /api/events?eventType=FAILED_LOGIN
GET /api/events?severity=HIGH&eventType=FAILED_LOGIN
```

### Pagination

```http
GET /api/events/page?page=0&size=20
```

## Health Monitoring

```http
GET /actuator/health
GET /actuator/info
```
## Observability

Sentinel exposes runtime metrics through Spring Boot Actuator:

```http
GET /actuator/metrics
```

Example:

```http
GET /actuator/metrics/jvm.memory.used
```

Application logs are emitted in structured JSON format to make them easier to process with log aggregation systems.

## Rate Limiting

`POST /api/events` is rate limited to protect the ingestion path from excessive request bursts.

When the configured limit is exceeded, Sentinel returns:

```http
429 Too Many Requests
```

## Critical Event Alerts

When a security event with severity `CRITICAL` is created, Sentinel can send the event as JSON to a configured webhook URL.

Configure it with:

```properties
sentinel.alert.webhook-url=https://example.com/webhook
```

If the property is blank, webhook delivery is disabled.

## Run with Docker

Start the complete application:

```bash
docker compose up -d --build
```

Check the containers:

```bash
docker compose ps
```

Test Sentinel:

```bash
curl http://localhost:8080/actuator/health
```

Stop the application:

```bash
docker compose down
```

PostgreSQL data is stored in a persistent Docker volume.

## Run Tests

Docker must be available because the integration tests use Testcontainers.

```bash
mvn test
```

## CI

GitHub Actions automatically:

1. checks out the repository
2. configures Java 21
3. runs the Maven test suite
4. verifies that the Docker image builds successfully

CI runs on pushes and pull requests to `main`.