## CI/CD Pipeline

### GitHub Actions Workflow

**File**: `.github/workflows/ci.yml`

**Triggers**:
- Push to `main` or `develop` branches
- Pull requests to `main` branch

**Pipeline Steps**:

```yaml
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - Checkout code
      - Set up JDK 21
      - Cache Gradle dependencies  
      - Run unit tests
      - Build Docker image
      - Test Docker image
      - Upload test results
```

### Local CI Testing

```bash
# Run the same checks as CI
./gradlew test                    # Unit tests
docker build -t test-image .      # Docker build test
docker run --rm test-image java -jar app.jar --help  # Docker run test
```
