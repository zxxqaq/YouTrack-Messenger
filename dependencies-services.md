### Core Dependencies

```kotlin
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
| **app** | Custom (Spring Boot) | Main application |
| **ngrok** | `ngrok/ngrok:latest` | Webhook tunnel |
| **webhook-setup** | `curlimages/curl:latest` | Automatic webhook configuration |
