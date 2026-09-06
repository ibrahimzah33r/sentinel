# Sentinel Architecture

## System Overview

```mermaid
flowchart LR
    U[User]
    B[Browser]
    W[React Frontend]
    N[Nginx]
    A[Spring Boot API]
    D[(PostgreSQL)]

    U --> B
    B --> N
    N --> W
    N --> A
    A --> D
```

Sentinel is a full-stack security event and case management application.

- **React** provides the analyst interface.
- **Nginx** routes frontend and `/api` requests.
- **Spring Boot** handles authentication, authorization and application logic.
- **PostgreSQL** stores analysts, security events and cases.

## Authentication

```mermaid
sequenceDiagram
    participant U as User
    participant W as React
    participant A as Spring Boot
    participant D as PostgreSQL

    U->>W: Enter credentials
    W->>A: GET /api/auth/csrf
    A-->>W: CSRF token
    W->>A: POST /api/auth/login
    A->>D: Find analyst
    D-->>A: Analyst
    A-->>W: Session + user details

    W->>A: GET /api/auth/me
    A-->>W: Current authenticated user
```

Authentication uses a server-side Spring Security session.

The browser stores a `JSESSIONID` cookie while the authenticated session remains on the backend.

## Authorization

```mermaid
flowchart TD
    R[API Request]
    S[Spring Security]
    A{Authenticated?}
    P{Required role?}
    C[Controller]
    X[401 Unauthorized]
    F[403 Forbidden]

    R --> S
    S --> A
    A -- No --> X
    A -- Yes --> P
    P -- Allowed --> C
    P -- Not allowed --> F
```

Sentinel currently supports:

- `ADMIN`
- `ANALYST`

Administrative endpoints under `/api/admin/**` require the `ADMIN` role.

## Database Migrations

```mermaid
flowchart LR
    M[Flyway migrations]
    D[(PostgreSQL)]
    H[Hibernate validation]
    J[JPA entities]

    M --> D
    D --> H
    J --> H
```

Flyway owns production schema changes.

Migration files live in:

```text
src/main/resources/db/migration/
```

Example:

```text
V1__baseline_schema.sql
V2__future_change.sql
V3__future_change.sql
```

Hibernate uses schema validation rather than automatically changing the production database.

## Deployment

```mermaid
flowchart TD
    G[GitHub]
    CI[GitHub Actions]
    GHCR[GitHub Container Registry]
    DC[Docker Compose]
    N[Nginx]
    W[Frontend Container]
    A[Backend Container]
    D[(PostgreSQL Volume)]

    G --> CI
    CI --> GHCR
    GHCR --> DC

    DC --> N
    DC --> W
    DC --> A
    DC --> D

    N --> W
    N --> A
    A --> D
```

GitHub Actions builds and publishes Docker images to GHCR.

Docker Compose runs the production-style stack and supplies environment-specific configuration through `.env`.

Secrets are not committed to Git.
