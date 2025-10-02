### Project Structure

```
youtrack_messenger/
├── .github/workflows/          # GitHub Actions CI/CD
│   └── ci.yml                 # CI pipeline configuration
├── docker/                    # Docker configurations
│   └── app/
│       └── application-docker.yml  # Docker environment config
├── src/
│   ├── main/java/org/example/
│   │   ├── app/               # Application entry point
│   │   │   └── Application.java
│   │   ├── application/       # Application layer
│   │   │   └── service/       # Business logic services
│   │   ├── domain/            # Domain layer
│   │   │   ├── model/         # Domain models
│   │   │   ├── port/          # Interface definitions
│   │   │   └── view/          # View models/DTOs
│   │   ├── infrastructure/    # Infrastructure layer
│   │   │   ├── scheduler/     # Scheduling configuration
│   │   │   ├── storage/       # Database implementations
│   │   │   ├── telegram/      # Telegram API client
│   │   │   └── youtrack/      # YouTrack API client
│   │   └── interfaces/        # Interface layer
│   │       └── rest/          # REST controllers
│   ├── main/resources/
│   │   └── application.yml    # Main configuration
│   └── test/java/             # Unit tests
├── build.gradle.kts           # Gradle build configuration
├── docker-compose.yml         # Docker services orchestration
├── Dockerfile                 # Docker image definition
├── manage.sh                  # Service management script
├── setup.sh                   # One-click setup script
├── port-check.sh             # Port conflict detection
├── configure-ports.sh        # Port configuration management
└── README-Docker.md          # Detailed Docker instructions
```

### Architecture Overview

The project follows **Hexagonal Architecture (Ports & Adapters)** pattern:

- **Domain Layer**: Core business logic, independent of external concerns
- **Application Layer**: Orchestrates domain services and use cases
- **Infrastructure Layer**: Implements external integrations (APIs, databases)
- **Interface Layer**: Exposes functionality via REST endpoints
