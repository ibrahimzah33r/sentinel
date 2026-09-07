# Sentinel

Sentinel is a full-stack security event and investigation case management application built as a portfolio project.

It demonstrates a production-style architecture with a React analyst console, Spring Boot API, PostgreSQL persistence, session-based authentication, role-based authorization, automated testing, Docker, database migrations and CI/CD.

## Features

- Security event creation, viewing, filtering and pagination
- Investigation case management
- Analyst authentication with server-side sessions
- CSRF protection
- `ADMIN` and `ANALYST` roles
- Admin analyst-account management
- Password resets
- Account enable/disable controls
- Safe analyst deletion
- Safe role promotion and demotion
- Protection against removing the last enabled administrator
- Immediate session revalidation after account disable, deletion or role change
- PostgreSQL persistence
- Flyway database migrations
- Spring Boot Actuator health monitoring
- Request rate limiting
- Structured API errors
- React analyst console
- Dockerized production-style deployment
- GitHub Actions CI/CD
- Docker images published to GitHub Container Registry

## Technology

### Backend

- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- Hibernate
- PostgreSQL
- Flyway
- Maven
- Testcontainers
- Spring Boot Actuator

### Frontend

- React
- TypeScript
- Vite

### Infrastructure

- Docker
- Docker Compose
- Nginx
- GitHub Actions
- GitHub Container Registry

## Repository Structure

```text
sentinel/
├── backend/
│   ├── src/
│   ├── pom.xml
│   ├── Dockerfile
│   └── compose.yaml
│
├── web/
│   ├── src/
│   ├── package.json
│   └── Dockerfile
│
├── deployment/
│   ├── docker-compose.prod.yml
│   └── nginx-proxy.conf
│
├── docs/
│   └── architecture.md
│
└── .github/
    └── workflows/
        ├── backend-ci.yml
        └── web-ci.yml
```

## Architecture

The application is split into a React frontend and Spring Boot backend.

```text
User
  ↓
Browser
  ↓
Nginx
  ├──→ React frontend
  │
  └──→ Spring Boot API
            ↓
        PostgreSQL
```

For more detail, see:

```text
docs/architecture.md
```

## Authentication and Authorization

Sentinel uses server-side Spring Security sessions.

After a successful login, the browser stores a `JSESSIONID` cookie. Authentication remains on the backend rather than being stored as a client-side access token.

Protected API routes require authentication.

Administrative routes under:

```text
/api/admin/**
```

require the `ADMIN` role.

Sentinel supports:

```text
ADMIN
ANALYST
```

Authenticated sessions are revalidated against the database. If an analyst is disabled or deleted, their session loses access. If their role changes, their authorization is refreshed for subsequent requests.

Administrative safeguards prevent disabling, deleting or demoting the last enabled administrator.

## Local Development

### Backend

From the repository root:

```bash
cd backend
docker compose up -d postgres
mvn spring-boot:run
```

The backend runs on:

```text
http://localhost:8080
```

### Frontend

In another terminal:

```bash
cd web
npm ci
npm run dev
```

The Vite development server runs on:

```text
http://localhost:5173
```

## Tests

Backend tests require Docker because integration tests use Testcontainers.

```bash
cd backend
mvn test
```

Build the frontend with:

```bash
cd web
npm run build
```

## Database Migrations

Flyway owns schema migrations.

Migration files are stored under:

```text
backend/src/main/resources/db/migration/
```

Hibernate validates the database schema rather than automatically modifying the production schema.

## Production Deployment

Production uses prebuilt Docker images published to GitHub Container Registry:

```text
ghcr.io/ibrahimzah33r/sentinel-backend:latest
ghcr.io/ibrahimzah33r/sentinel-web:latest
```

Environment-specific secrets are supplied through:

```text
deployment/.env
```

The `.env` file is intentionally excluded from Git.

### Pull current images

From the repository root:

```bash
docker compose \
  -p sentinel \
  --env-file deployment/.env \
  -f deployment/docker-compose.prod.yml \
  pull
```

### Start or update the stack

```bash
docker compose \
  -p sentinel \
  --env-file deployment/.env \
  -f deployment/docker-compose.prod.yml \
  up -d
```

### Check status

```bash
docker compose \
  -p sentinel \
  --env-file deployment/.env \
  -f deployment/docker-compose.prod.yml \
  ps
```

### View logs

```bash
docker compose \
  -p sentinel \
  --env-file deployment/.env \
  -f deployment/docker-compose.prod.yml \
  logs -f
```

### Stop the stack

```bash
docker compose \
  -p sentinel \
  --env-file deployment/.env \
  -f deployment/docker-compose.prod.yml \
  down
```

Do not use `down -v` unless the PostgreSQL data volume is intentionally being removed.

## CI/CD

The monorepo contains separate workflows for the backend and frontend.

Backend changes:

```text
backend/**
→ Maven tests
→ backend Docker build
→ sentinel-backend image published to GHCR
```

Frontend changes:

```text
web/**
→ npm build
→ frontend Docker build
→ sentinel-web image published to GHCR
```

The source code is maintained in one Git repository while the backend and frontend remain separate deployable Docker images.
