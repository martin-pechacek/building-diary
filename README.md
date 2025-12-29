# Building Diary

[![CircleCI](https://dl.circleci.com/status-badge/img/gh/martin-pechacek/building-diary/tree/main.svg?style=svg)](https://dl.circleci.com/status-badge/redirect/gh/martin-pechacek/building-diary/tree/main)

A web application for tracking construction and building project progress.

## Tech Stack

- **Framework:** Spring Boot 4.0.0
- **Language:** Java 25
- **Build Tool:** Gradle (Kotlin DSL)
- **Database:** PostgreSQL
- **ORM:** Spring Data JPA
- **Migrations:** Flyway
- **Authentication:** Spring Security
- **Session Storage:** Redis
- **Web:** Spring MVC

## Prerequisites

- Java 25+
- Docker and Docker Compose

## Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/martin-pechacek/building-diary
   cd building-diary
   ```

2. Start the infrastructure services:
   ```bash
   docker compose -f docker/docker-compose.yml up -d
   ```

3. Run the application:
   ```bash
   ./gradlew bootRun
   ```

## Docker Setup

The project includes a Docker Compose configuration in the `docker/` folder for local development with the following services:

| Service    | Port | Credentials                    | Purpose              |
|------------|------|--------------------------------|----------------------|
| PostgreSQL | 5432 | `building_diary:building_diary`| Primary database     |
| Redis      | 6379 | -                              | Session storage      |
| Keycloak   | 8180 | `admin:admin`                  | Authentication server|

### Commands

Start all services:
```bash
docker compose -f docker/docker-compose.yml up -d
```

Stop all services:
```bash
docker compose -f docker/docker-compose.yml down
```

Stop and remove volumes (reset data):
```bash
docker compose -f docker/docker-compose.yml down -v
```

View logs:
```bash
docker compose -f docker/docker-compose.yml logs -f [service_name]
```

Check service health:
```bash
docker compose -f docker/docker-compose.yml ps
```

### Data Persistence

All service data is persisted in Docker volumes:
- `postgres_data` - PostgreSQL database files
- `redis_data` - Redis append-only file
- `keycloak_data` - Keycloak data

### Keycloak Admin Console

Access the Keycloak admin console at http://localhost:8180 with credentials `admin:admin`.

### Keycloak Setup

After starting Keycloak for the first time, you need to configure the realm, roles, and client.

#### 1. Create Realm

1. Login to Keycloak Admin Console at http://localhost:8180
2. Click the dropdown in the top-left (shows "master")
3. Click **Create realm**
4. Set **Realm name** to `building-diary`
5. Click **Create**

#### 2. Create Roles

1. In the `building-diary` realm, go to **Realm roles** in the left menu
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
   - **Client ID:** `building-diary-client`
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
   - **Client ID:** `building-diary-admin`
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
7. Click **Assign role** -> *Client roles*
8. Search for `realm-management` and assign these roles:
   - `manage-users`
   - `view-users`
   - `query-users`
9. Go to the **Credentials** tab
10. Copy the **Client secret** and set it as environment variable or in application.yaml:
    ```yaml
    keycloak:
      admin:
        client-secret: <your-admin-client-secret>
    ```

#### 6. Create Test Users

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
curl -X POST "http://localhost:8180/realms/building-diary/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=building-diary-client" \
  -d "client_secret=<your-client-secret>" \
  -d "username=<test-user>" \
  -d "password=<test-password>" \
  -d "grant_type=password"
```

To decode the JWT token, use https://jwt.io or:

```bash
# Extract and decode the access_token (requires jq)
TOKEN=$(curl -s -X POST "http://localhost:8180/realms/building-diary/protocol/openid-connect/token" \
  -d "client_id=building-diary-client" \
  -d "client_secret=<your-client-secret>" \
  -d "username=<test-user>" \
  -d "password=<test-password>" \
  -d "grant_type=password" | jq -r '.access_token')

# Decode the payload
echo $TOKEN | cut -d'.' -f2 | base64 -d 2>/dev/null | jq
```

## Development

Build the project:
```bash
./gradlew build
```

Run tests:
```bash
./gradlew test
```

## Configuration

The application expects the following services:

| Service    | Default Port | Purpose          |
|------------|--------------|------------------|
| PostgreSQL | 5432         | Primary database |
| Redis      | 6379         | Session storage  |

Configure connection details in `application.yaml` or via environment variables.

## API Documentation

| Endpoint | Description |
|----------|-------------|
| `/swagger-ui.html` | Interactive API documentation |
| `/api-docs` | OpenAPI JSON specification |

## Authentication

### Auth Flow (BFF Pattern)

The application uses a Backend-for-Frontend (BFF) pattern where JWT tokens are stored server-side in Redis sessions, not exposed to the client.

```
┌─────────┐         ┌─────────────┐         ┌──────────┐         ┌───────┐
│ Client  │         │   Backend   │         │ Keycloak │         │ Redis │
└────┬────┘         └──────┬──────┘         └────┬─────┘         └───┬───┘
     │                     │                     │                   │
     │ POST /auth/login    │                     │                   │
     │ {email, password}   │                     │                   │
     │────────────────────>│                     │                   │
     │                     │                     │                   │
     │                     │ Token Request       │                   │
     │                     │ (password grant)    │                   │
     │                     │────────────────────>│                   │
     │                     │                     │                   │
     │                     │ JWT Tokens          │                   │
     │                     │<────────────────────│                   │
     │                     │                     │                   │
     │                     │ Store session       │                   │
     │                     │ (tokens + claims)   │                   │
     │                     │─────────────────────────────────────────>
     │                     │                     │                   │
     │ {username, roles}   │                     │                   │
     │ + Session Cookie    │                     │                   │
     │<────────────────────│                     │                   │
     │                     │                     │                   │
     │ GET /api/resource   │                     │                   │
     │ (with cookie)       │                     │                   │
     │────────────────────>│                     │                   │
     │                     │                     │                   │
     │                     │ Check session       │                   │
     │                     │ expiration only     │                   │
     │                     │<─────────────────────────────────────────
     │                     │                     │                   │
     │ Response            │                     │                   │
     │<────────────────────│                     │                   │
```

**Key points:**
- Client never sees JWT tokens
- Session stores: username, email, roles, token expiration, tokens (for refresh)
- On each request: only expiration timestamp is checked (no Keycloak call)
- On refresh: re-validates with Keycloak

### Auth Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/auth/register` | POST | Register new user |
| `/api/v1/auth/login` | POST | Authenticate and create session |
| `/api/v1/auth/refresh` | POST | Refresh session tokens |

### Keycloak Clients

The application uses two separate Keycloak clients with different purposes:

| Client | Purpose | Grant Type | Used For |
|--------|---------|------------|----------|
| `building-diary-admin` | User management | Client Credentials | Creating/deleting users during registration |
| `building-diary-client` | User authentication | Password Grant | Login and token refresh |

#### building-diary-admin

Service account client for backend-to-Keycloak communication:
- **Authentication flow:** Service accounts only
- **Permissions:** `manage-users`, `view-users`, `query-users`
- **Usage:** Called by `KeycloakServiceImpl.createUser()` and `deleteUser()`

#### building-diary-client

Public-facing client for user authentication:
- **Authentication flow:** Direct access grants (password grant)
- **Usage:** Called by `KeycloakServiceImpl.authenticate()` and `refreshToken()`
- **Security:** Client secret stored server-side, never exposed to frontend