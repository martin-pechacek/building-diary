# Building Diary

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
- PostgreSQL
- Redis

## Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/martin-pechacek/building-diary
   cd building-diary
   ```

2. Configure the database and Redis connection in `src/main/resources/application.yaml`

3. Run the application:
   ```bash
   ./gradlew bootRun
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