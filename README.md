# Construction Site Diary

[![CircleCI](https://dl.circleci.com/status-badge/img/gh/martin-pechacek/construction-site-diary/tree/main.svg?style=svg&circle-token=CCIPRJ_25iU6VBaMn7XQu7UXcFz43_9e21b05418cdd4a87f03081206b34464189bd18f)](https://dl.circleci.com/status-badge/redirect/gh/martin-pechacek/construction-site-diary/tree/main)

A microservice-based web application for tracking construction and building project progress.

**DISCLAIMER**: Documentation and tests were generated with the help of AI.

## Architecture

The application follows a microservice architecture, organized as a multi-module Gradle monorepo. Each service has its own database, runs independently, and communicates via REST or message queue. Authentication is shared through a common Keycloak realm and JWT tokens.

### Service Overview

| Service | Module | Port | Database | Description |
|---------|--------|------|----------|-------------|
| Core Service | `construction-site-diary-core` | 8080 | `construction_site_diary` | Projects, diary entries, authentication, export |
| Photos Service | `photos-service` | 8081 | `photos` | Photo upload, download, management |
| Notification Service | `notification-service` | 8082 | `notifications` | Async email notifications |

### Infrastructure

| Service | Port | Purpose |
|---------|------|---------|
| PostgreSQL | 5432 | Databases for core, photos, and notification services |
| Redis | 6379 | Caching |
| Keycloak | 8180 | Authentication and authorization |
| RabbitMQ | 5672 / 15672 | Message queue / Management UI |
| Mailpit | 1025 / 8025 | SMTP (dev) / Email inbox UI |

## Table of Contents

- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Setup](#setup)
- [Docker Setup](#docker-setup)
  - [Commands](#commands)
  - [Data Persistence](#data-persistence)
  - [Keycloak Admin Console](#keycloak-admin-console)
  - [Keycloak Setup](#keycloak-setup)
  - [Testing JWT Retrieval](#testing-jwt-retrieval)
- [Development](#development)
- [Configuration](#configuration)
- [API Documentation](#api-documentation)
- [Authentication](#authentication)
  - [Auth Flow (Stateless JWT)](#auth-flow-stateless-jwt)
  - [Auth Endpoints](#auth-endpoints)
  - [Keycloak Clients](#keycloak-clients)
- [Construction Projects](#construction-projects)
  - [Project Lifecycle](#project-lifecycle)
  - [State Transition Guards](#state-transition-guards)
  - [Address Validation](#address-validation)
- [Diary Export](#diary-export)
- [Photo Upload](#photo-upload)
- [Weather Integration](#weather-integration)
- [Notification Service](#notification-service)

## Tech Stack

- **Framework:** Spring Boot 4.0.0
- **Language:** Java 25
- **Build Tool:** Gradle (Kotlin DSL)
- **Database:** PostgreSQL
- **ORM:** Spring Data JPA (Hibernate)
- **Migrations:** Flyway
- **Authentication:** Spring Security + JWT (Keycloak)
- **Caching:** Redis

## Prerequisites

- Java 25+
- Docker and Docker Compose

## Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/martin-pechacek/construction-site-diary
   cd construction-site-diary
   ```

2. Start the infrastructure services:
   ```bash
   docker compose -f docker/docker-compose.yml up -d
   ```

3. Run the core application:
   ```bash
   ./gradlew :construction-site-diary-core:bootRun
   ```

4. Run the photos service:
   ```bash
   ./gradlew :photos-service:bootRun
   ```

5. Run the notification service:
   ```bash
   ./gradlew :notification-service:bootRun
   ```

## Docker Setup

The project includes a Docker Compose configuration in the `docker/` folder for local development with the following services:

| Service    | Port | Credentials                    | Purpose                       |
|------------|------|--------------------------------|-------------------------------|
| PostgreSQL | 5432 | `construction_site_diary:construction_site_diary` | Core database        |
| PostgreSQL | 5432 | `photos_user:photos_user`      | Photos database               |
| PostgreSQL | 5432 | `notifications_user:notifications_pass` | Notifications database |
| Redis      | 6379 | -                              | Caching                       |
| Keycloak   | 8180 | `admin:admin`                  | Authentication server         |
| RabbitMQ   | 5672 / 15672 | `construction_site_diary:diary` | Message queue / Management UI |
| Mailpit    | 1025 / 8025 | -                             | SMTP (dev) / Email inbox UI   |

### Commands

Start all services:
```bash
docker compose -f docker/docker-compose.yml up -d
```

View logs:
```bash
docker compose -f docker/docker-compose.yml logs -f [service_name]
```

### Data Persistence

All service data is persisted in Docker volumes:
- `postgres_data` - PostgreSQL database files
- `redis_data` - Redis append-only file
- `keycloak_data` - Keycloak data
- `rabbitmq_data` - RabbitMQ queue data

### Keycloak Admin Console

Access the Keycloak admin console at http://localhost:8180 with credentials `admin:admin`.

### Keycloak Setup

After starting Keycloak for the first time, you need to configure the realm, roles, and client.

#### 1. Create Realm

1. Login to Keycloak Admin Console at http://localhost:8180
2. Click the dropdown in the top-left (shows "master")
3. Click **Create realm**
4. Set **Realm name** to `construction-site-diary`
5. Click **Create**

#### 2. Create Roles

1. In the `construction-site-diary` realm, go to **Realm roles** in the left menu
2. Click **Create role**
3. Create role `USER`:
   - **Role name:** `USER`
   - Click **Save**
4. Click **Create role** again
5. Create role `ADMIN`:
   - **Role name:** `ADMIN`
   - Click **Save**

#### 3. Create Client

1. Go to **Clients** in the left menu
2. Click **Create client**
3. **General Settings:**
   - **Client type:** OpenID Connect
   - **Client ID:** `construction-site-diary-client`
   - Click **Next**
4. **Capability config:**
   - **Client authentication:** ON (confidential client)
   - **Authorization:** OFF
   - **Authentication flow:** Check "Standard flow" and "Direct access grants"
   - Click **Next**
5. **Login settings:**
   - **Root URL:** `http://localhost:8080`
   - **Home URL:** `http://localhost:8080`
   - **Valid redirect URIs:** `http://localhost:8080/*`
   - **Valid post logout redirect URIs:** `http://localhost:8080/*`
   - **Web origins:** `http://localhost:8080`
   - Click **Save**

#### 4. Get Client Secret

1. In the client settings, go to the **Credentials** tab
2. Copy the **Client secret**
3. Paste it into `src/main/resources/application.yaml` under `spring.security.oauth2.client.registration.keycloak.client-secret`

#### 5. Create Admin Client

This client is used by the Spring Boot backend to create users in Keycloak.

1. Go to **Clients** in the left menu
2. Click **Create client**
3. **General Settings:**
   - **Client type:** OpenID Connect
   - **Client ID:** `construction-site-diary-admin`
   - Click **Next**
4. **Capability config:**
   - **Client authentication:** ON
   - **Authorization:** OFF
   - **Authentication flow:** Uncheck all, check only **Service accounts roles**
   - Click **Next**
5. **Login settings:**
   - Leave all fields empty
   - Click **Save**
6. Go to **Service account roles** tab
7. Click **Assign role** -> change filter to **"Filter by clients"**
8. Search for `realm-management` and assign these roles:
   - `manage-users`
   - `view-users`
   - `query-users`
   - `view-realm`
9. Go to the **Credentials** tab
10. Copy the **Client secret** and set it as environment variable or in application.yaml:
    ```yaml
    keycloak:
      admin:
        client-secret: <your-admin-client-secret>
    ```

#### 6. Disable Required Actions

Keycloak 26+ enables "Verify Email" and "Verify Profile" by default, which prevents API-created users from logging in via password grant.

1. Go to **Authentication** -> **Required actions**
2. Disable **Verify Email** (toggle off "Set as default action")
3. Disable **Verify Profile** (toggle off "Set as default action")

#### 7. Create Test Users

1. Go to **Users** in the left menu
2. Click **Create new user**
3. Fill in the required fields:
   - **Username:** e.g. `test_user`
   - **Email:** e.g. `test@example.com`
   - **First name:** e.g. `Test`
   - **Last name:** e.g. `User`
4. Click **Create**
5. Go to the **Credentials** tab
6. Click **Set password**
7. Enter the password and set **Temporary** to **OFF**
8. Click **Save**
9. Go to the **Role mapping** tab
10. Click **Assign role**
11. Filter by realm roles and assign `USER` or `ADMIN` role

### Testing JWT Retrieval

You can test JWT token retrieval using curl:

```bash
# Get token using Resource Owner Password Credentials (for testing only)
curl -X POST "http://localhost:8180/realms/construction-site-diary/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=construction-site-diary-client" \
  -d "client_secret=<your-client-secret>" \
  -d "username=<test-user>" \
  -d "password=<test-password>" \
  -d "grant_type=password"
```

To decode the JWT token, use https://jwt.io or:

```bash
# Extract and decode the access_token (requires jq)
TOKEN=$(curl -s -X POST "http://localhost:8180/realms/construction-site-diary/protocol/openid-connect/token" \
  -d "client_id=construction-site-diary-client" \
  -d "client_secret=<your-client-secret>" \
  -d "username=<test-user>" \
  -d "password=<test-password>" \
  -d "grant_type=password" | jq -r '.access_token')

# Decode the payload
echo $TOKEN | cut -d'.' -f2 | base64 -d 2>/dev/null | jq
```

## Development

Build all modules:
```bash
./gradlew build
```

Build a specific module:
```bash
./gradlew :construction-site-diary-core:build
./gradlew :photos-service:build
./gradlew :notification-service:build
```

Run tests:
```bash
./gradlew test
```

## Configuration

The application expects the following services:

| Service    | Default Port | Purpose                    |
|------------|--------------|----------------------------|
| PostgreSQL | 5432         | Core database (`construction_site_diary`) |
| PostgreSQL | 5432         | Photos database (`photos`) |
| PostgreSQL | 5432         | Notifications database (`notifications`) |
| Redis      | 6379         | Caching                    |
| Keycloak   | 8180         | Authentication server      |
| RabbitMQ   | 5672 / 15672 | Message queue / Management UI |
| Mailpit    | 1025 / 8025  | Email delivery / inbox (dev) |
| Notification Service | 8082 | Notification service     |

Configure connection details in `application.yaml` or via environment variables.

## API Documentation

| Service | Swagger UI | OpenAPI JSON |
|---------|------------|--------------|
| Core (port 8080) | `/swagger-ui.html` | `/api-docs` |
| Photos (port 8081) | `/swagger-ui.html` | `/api-docs` |

## Authentication

### Auth Flow (Stateless JWT)

The application uses stateless JWT authentication. Tokens are issued by Keycloak and returned directly to the client.

```
┌─────────┐         ┌─────────────┐         ┌──────────┐
│ Client  │         │   Backend   │         │ Keycloak │
└────┬────┘         └──────┬──────┘         └────┬─────┘
     │                     │                     │
     │ POST /auth/login    │                     │
     │ {email, password}   │                     │
     │────────────────────>│                     │
     │                     │                     │
     │                     │ Token Request       │
     │                     │ (password grant)    │
     │                     │────────────────────>│
     │                     │                     │
     │                     │ JWT Tokens          │
     │                     │<────────────────────│
     │                     │                     │
     │ {email, accessToken,│                     │
     │  refreshToken, roles}                     │
     │<────────────────────│                     │
     │                     │                     │
     │ GET /api/resource   │                     │
     │ Authorization:      │                     │
     │ Bearer <token>      │                     │
     │────────────────────>│                     │
     │                     │                     │
     │                     │ Validate JWT        │
     │                     │ (decode claims)     │
     │                     │                     │
     │ Response            │                     │
     │<────────────────────│                     │
```

**Key points:**
- Client receives and stores JWT tokens (access + refresh)
- Stateless authentication - no server-side session storage
- Access token sent via `Authorization: Bearer <token>` header
- Token validation done locally by decoding JWT claims
- Refresh token sent via `X-Refresh-Token` header to `/auth/refresh`

### Auth Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/auth/register` | POST | Register new user |
| `/api/v1/auth/login` | POST | Authenticate and get JWT tokens |
| `/api/v1/auth/refresh` | POST | Refresh tokens (via `X-Refresh-Token` header) |

### Keycloak Clients

The application uses two separate Keycloak clients with different purposes:

| Client | Purpose | Grant Type | Used For |
|--------|---------|------------|----------|
| `construction-site-diary-admin` | User management | Client Credentials | Creating/deleting users during registration |
| `construction-site-diary-client` | User authentication | Password Grant | Login and token refresh |

#### construction-site-diary-admin

Service account client for backend-to-Keycloak communication:
- **Authentication flow:** Service accounts only
- **Permissions:** `manage-users`, `view-users`, `query-users`
- **Usage:** Called by `KeycloakService.createUser()` and `deleteUser()`

#### construction-site-diary-client

Public-facing client for user authentication:
- **Authentication flow:** Direct access grants (password grant)
- **Usage:** Called by `KeycloakService.authenticate()` and `refreshToken()`
- **Security:** Client secret stored server-side, never exposed to frontend

## Construction Projects

Projects represent construction/building projects with state machine-driven lifecycle.

### Project Lifecycle

Projects follow a state machine with three states:

```
┌──────────┐    START_WORK    ┌─────────────┐    COMPLETE    ┌───────────┐
│ PLANNING │─────────────────>│ IN_PROGRESS │───────────────>│ COMPLETED │
└──────────┘                  └─────────────┘                └───────────┘
```

### State Transition Guards

#### Start Work (PLANNING → IN_PROGRESS)

Requirements:
- Construction manager assigned
- Construction site address set
- Permit number filled

#### Complete (IN_PROGRESS → COMPLETED)

Requirements:
- All diary entries filled (including non-working days)

### Address Validation

Address must have either:
- **Parcel number** (eg new constructions), OR
- **Street + street number** (eg reconstructions)

Plus required fields: city, postal code, country (CZ or SK).

## Diary Export

Diary entries from a project can be exported to CSV or PDF format.

### Export Content

Both formats include:
- Project name and date range
- Entry date and summary
- Weather conditions and temperature
- Workforce entries (role, name, hours)
- Material usage (name, quantity, unit)

## Photo Upload

Photos are managed by a separate **photos-service** (port 8081). Photos are linked to diary entries via `diaryEntryId` (soft reference — no foreign key across databases).

**Allowed file types:** JPEG and PNG only (validated by file signature)

### Photo Endpoints (port 8081)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/photos/projects/{projectId}/diary-entries/{diaryEntryId}` | Upload a photo |
| `GET` | `/api/v1/photos/{id}` | Download a photo |
| `DELETE` | `/api/v1/photos/{id}` | Delete a photo |
| `GET` | `/api/v1/photos/diary-entries/{diaryEntryId}` | List photos for a diary entry |
| `DELETE` | `/api/v1/photos/diary-entries/{diaryEntryId}` | Delete all photos for a diary entry |

### Access Control

Each photo stores `projectId` and `ownerUserId`. Users can only access their own photos. Admins (`ROLE_ADMIN`) can access all photos. Authorization is self-contained — no inter-service calls.

### Configuration

Configure photo storage location in `photos-service/src/main/resources/application.yaml`:

```yaml
storage:
  location: ./uploads-photos
```

## Weather Integration

Diary entries can automatically fetch weather data for the construction site location using the [Open-Meteo API](https://open-meteo.com/).

### How It Works

```
┌─────────┐         ┌─────────────┐         ┌─────────────┐         ┌─────────────┐
│ Client  │         │   Backend   │         │  Geocoding  │         │  Weather    │
│         │         │             │         │     API     │         │     API     │
└────┬────┘         └──────┬──────┘         └──────┬──────┘         └──────┬──────┘
     │                     │                       │                       │
     │ Create diary entry  │                       │                       │
     │ (city, country,date)│                       │                       │
     │────────────────────>│                       │                       │
     │                     │                       │                       │
     │                     │ Get coordinates       │                       │
     │                     │ for city/country      │                       │
     │                     │──────────────────────>│                       │
     │                     │                       │                       │
     │                     │ {lat, lon}            │                       │
     │                     │<──────────────────────│                       │
     │                     │                       │                       │
     │                     │ Get weather for       │                       │
     │                     │ coordinates + date    │                       │
     │                     │──────────────────────────────────────────────>│
     │                     │                       │                       │
     │                     │ {temperature,         │                       │
     │                     │  weather_code}        │                       │
     │                     │<──────────────────────────────────────────────│
     │                     │                       │                       │
     │ Entry with weather  │                       │                       │
     │<────────────────────│                       │                       │
```

### Features

- **Automatic geocoding:** City name is converted to coordinates via Open-Meteo Geocoding API
- **Historical weather:** Past dates fetch from the Archive API
- **Forecast weather:** Current/future dates fetch from the Forecast API
- **Caching:** Results are cached in Redis to reduce API calls
- **No API key required:** Open-Meteo is free and doesn't require authentication

### Weather Data

| Field | Description |
|-------|-------------|
| `temperature` | Mean daily temperature in Celsius |
| `condition` | Weather condition (Clear, Cloudy, Rain, Snow, etc.) |

### Supported Countries

Weather lookup works for cities in Czech Republic (CZ) and Slovakia (SK).

## Notification Service

The notification service delivers transactional emails for key application events. It runs as an independent service on port 8082.

### Emails Sent

| Trigger | Recipients |
|---------|------------|
| User registration | Registering user — email verification link |
| Project started | Project owner + construction manager (if assigned and different) |
| Project completed | Project owner + construction manager (if assigned and different) |

### Local Development — Viewing Emails

In development, all outgoing emails are captured by **Mailpit** instead of being delivered. Access the email inbox at **http://localhost:8025** after starting the Docker services.

### RabbitMQ Management UI

Queue status and message flow can be monitored at **http://localhost:15672** (credentials: `construction_site_diary` / `diary`).