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