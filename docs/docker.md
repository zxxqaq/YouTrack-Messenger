# Docker Service Management & Troubleshooting

This document provides detailed instructions for managing Docker services and troubleshooting common issues with the YouTrack Messenger Bot.

## Service Management

### Basic Operations
```bash
# Start all services
./manage.sh start

# Stop all services  
./manage.sh stop

# Restart all services
./manage.sh restart

# Check service status and URLs
./manage.sh status

# View application logs
./manage.sh logs
```

### Port Management
```bash
# Show port configuration and usage
./configure-ports.sh show

# Change port configuration
./configure-ports.sh change

# Check for port conflicts
./port-check.sh
```

## Troubleshooting

### Common Issues

**Port Conflicts**
```bash
# Manually kill processes on ports
lsof -ti:8080 | xargs kill -9
lsof -ti:4040 | xargs kill -9
```

**Service Health Check**
```bash
# Check service status
docker-compose ps

# View detailed logs
docker-compose logs -f app

# Check application health endpoint
curl http://localhost:8080/actuator/health
```

**Clean Restart**
```bash
# Stop all services
docker-compose down

# Remove containers and images
docker-compose down --rmi all

# Start fresh
./setup.sh
```

### Advanced Troubleshooting

**Database Issues**
```bash
# Check database files
ls -la data/

# Reset database (WARNING: deletes all data)
rm -rf data/ && mkdir -p data

# Check database connections in logs
docker-compose logs app | grep -i "hikari\|database\|h2"
```

**Network Issues**
```bash
# Check Docker networks
docker network ls

# Inspect network configuration
docker network inspect youtrack_messenger_default

# Test internal connectivity
docker-compose exec app ping ngrok
```

**Configuration Issues**
```bash
# Check environment variables
docker-compose config

# Verify .env file loading
docker-compose exec app env | grep -E "(YOUTRACK|TELEGRAM|NGROK)"

# Test API connectivity
docker-compose exec app curl -I $YOUTRACK_BASE_URL
```

## Local Development and Updating

### Development Commands
```bash
# Run tests locally
./gradlew test

# Build application
./gradlew build

# Run application locally (without Docker)
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

### Updating Services
```bash
# Update code and restart services
./manage.sh update

# Manual update process
git pull
docker-compose up -d --build
```

### Development with Docker
```bash
# Build Docker image locally
docker build -t youtrack-messenger:dev .

# Run with custom image
docker-compose -f docker-compose.yml up -d
```

## Maintenance

### Regular Maintenance
```bash
# Clean up Docker resources
./manage.sh clean

# Check resource usage
docker stats
docker system df

# Prune unused resources
docker system prune -f
```

### Data Management
```bash
# Backup database
cp -r data/ backup/data-$(date +%Y%m%d)

# View database size
du -sh data/

# Clean old logs
docker-compose logs --tail=100 app > recent_logs.txt
```

### System Monitoring
```bash
# Monitor container resource usage
docker-compose top

# Check container health
docker-compose ps

# View system resource usage
docker stats --no-stream
```

## Support

For issues and questions:
1. Check logs: `./manage.sh logs`
2. Verify status: `./manage.sh status`
3. Check ports: `./configure-ports.sh show`
4. Clean restart: `./manage.sh clean && ./setup.sh`
5. Check health: `curl http://localhost:8080/actuator/health`