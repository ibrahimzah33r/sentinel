# Sentinel Architecture

## System Overview

```mermaid
flowchart LR
    U[User]
    B[Browser]
    N[Nginx]
    W[React Frontend]
    A[Spring Boot API]
    D[(PostgreSQL)]

    U --> B
    B --> N
    N --> W
    N --> A
    A --> D
```

Sentinel is a full-stack security event and investigation case management application.

- **React** provides the analyst interface.
- **Nginx** serves as the production entry point and routes frontend and API traffic.
- **Spring Boot** handles authentication, authorization and application logic.
- **PostgreSQL** stores analysts, security events and investigation cases.

The source code is maintained in a monorepo:

```text
sentinel/
├── backend/
├── web/
├── deployment/
└── docs/
```

## Backend Architecture

```mermaid
flowchart LR
    R[HTTP Request]
    S[Spring Security]
    C[Controller]
    V[Service]
    P[Repository]
    D[(PostgreSQL)]

    R --> S
    S --> C
    C --> V
    V --> P
    P --> D
```

Controllers handle HTTP requests and responses.

Services contain application and business rules.

Repositories provide persistence through Spring Data JPA.

DTOs separate the external API contract from persistence entities.

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
    D-->>A: Analyst record
    A-->>W: Session + user details

    W->>A: GET /api/auth/me
    A-->>W: Current authenticated user
```

Authentication uses a server-side Spring Security session.

The browser stores a `JSESSIONID` cookie while authentication state remains on the backend.

CSRF protection is used for state-changing authenticated requests.

## Session Revalidation

Authenticated requests are revalidated against the current analyst record in PostgreSQL.

```mermaid
flowchart TD
    R[Authenticated request]
    F[AuthenticatedAnalystFilter]
    D[(Analyst record)]
    E{Account exists and enabled?}
    P{Role still matches session?}
    U[Continue request]
    X[Invalidate session and return 401]
    A[Refresh authorities]

    R --> F
    F --> D
    D --> E

    E -- No --> X
    E -- Yes --> P

    P -- Yes --> U
    P -- No --> A
    A --> U
```

This means:

```text
deleted analyst
→ session invalidated
→ 401 Unauthorized
```

```text
disabled analyst
→ session invalidated
→ 401 Unauthorized
```

```text
role changed
→ authorities refreshed
→ subsequent authorization uses the current database role
```

A demoted administrator therefore cannot continue using stale administrator privileges from an existing session.

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

Sentinel supports:

- `ADMIN`
- `ANALYST`

Routes under:

```text
/api/admin/**
```

require the `ADMIN` role.

Administrative account-management rules also ensure that at least one enabled administrator remains.

The final enabled administrator cannot be:

- disabled
- deleted
- demoted to `ANALYST`

## Database

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

Flyway owns schema changes.

Migration files live under:

```text
backend/src/main/resources/db/migration/
```

Hibernate validates the schema rather than automatically modifying the production database.

This keeps schema changes explicit and version controlled.

## Frontend

The React frontend acts as the analyst console.

It communicates with the backend through the `/api` routes and uses the authenticated browser session for protected operations.

Administrative UI controls are shown according to the authenticated user's role.

The frontend also prevents obviously invalid administrator actions in the interface, such as disabling, deleting or demoting the final enabled administrator.

Backend validation remains authoritative.

## Deployment

```mermaid
flowchart TD
    G[Sentinel GitHub Repository]
    CI[GitHub Actions]
    BIMG[Backend GHCR Image]
    WIMG[Web GHCR Image]
    DC[Docker Compose]
    N[Nginx]
    W[Frontend Container]
    A[Backend Container]
    D[(PostgreSQL Volume)]

    G --> CI

    CI --> BIMG
    CI --> WIMG

    BIMG --> DC
    WIMG --> DC

    DC --> N
    DC --> W
    DC --> A
    DC --> D

    N --> W
    N --> A
    A --> D
```

The source code lives in one GitHub monorepo, while the backend and frontend are built as independent Docker images:

```text
ghcr.io/ibrahimzah33r/sentinel-backend
ghcr.io/ibrahimzah33r/sentinel-web
```

Docker Compose pulls those images and runs:

```text
PostgreSQL
Backend
Frontend
Nginx
```

Production configuration is stored under:

```text
deployment/
├── docker-compose.prod.yml
└── nginx-proxy.conf
```

Environment-specific secrets are supplied through:

```text
deployment/.env
```

Secrets are not committed to Git.

## CI/CD

The monorepo contains separate GitHub Actions workflows for each deployable component.

```text
backend change
→ backend workflow
→ Maven tests
→ Docker build
→ sentinel-backend image
```

```text
web change
→ web workflow
→ npm build
→ Docker build
→ sentinel-web image
```

Path filters prevent unrelated backend or frontend changes from unnecessarily triggering both pipelines.

## Production Request Flow

```mermaid
flowchart LR
    U[User]
    N[Nginx]
    W[React Frontend]
    S[Spring Security]
    C[Spring Controllers]
    V[Services]
    R[Repositories]
    D[(PostgreSQL)]

    U --> N
    N --> W
    N --> S
    S --> C
    C --> V
    V --> R
    R --> D
```

This keeps the frontend, security layer, application logic and persistence responsibilities clearly separated.
