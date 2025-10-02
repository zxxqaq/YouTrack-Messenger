### Runtime Requirements

- **Java**: JDK 21
- **Build Tool**: Gradle 8.5+
- **Spring Boot**: 3.3.4

### Core Dependencies

```kotlin
plugins {
    id("org.springframework.boot") version "3.3.4"
    id("io.spring.dependency-management") version "1.1.6"
    id("java")
}

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
}

dependencies {
    // Spring Boot Framework
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // Database
    implementation("com.h2database:h2")  // H2 embedded database

    // HTTP Client
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.github.tomakehurst:wiremock-jre8:2.35.2")
}
```

### External Services

| Service | Purpose | Configuration |
|---------|---------|---------------|
| **YouTrack** | Issue tracking system | API token, Base URL |
| **Telegram Bot API** | Messaging platform | Bot token, Chat ID |
| **ngrok** | Local tunnel for webhooks | Authtoken |
| **H2 Database** | Data persistence | File-based storage |

### Docker Services

| Container | Image | Purpose |
|-----------|-------|---------|
| **app** | Custom (gradle:8.5-jdk21 → openjdk:21-jdk-slim) | Main Spring Boot application |
| **ngrok** | `ngrok/ngrok:latest` | Webhook tunnel |
| **webhook-setup** | `curlimages/curl:latest` | Automatic webhook configuration |

### Docker Build Dependencies

**Build Stage:**
- Base Image: `gradle:8.5-jdk21`
- Build Tool: Gradle 8.5 with JDK 21

**Runtime Stage:**
- Base Image: `openjdk:21-jdk-slim`
- System Tools: `curl`, `jq`
- Runtime: Java 21
