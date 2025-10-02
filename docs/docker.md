# YouTrack Messenger Bot - Docker Edition

This project implements a Telegram bot that integrates with YouTrack to provide notifications and interactive issue creation, now fully containerized with Docker for easy deployment.

## 🚀 Quick Start

### Prerequisites
- Docker
- Docker Compose
- YouTrack instance with API token
- Telegram bot token
- ngrok authtoken

### One-Click Setup

```bash
# 1. Clone the repository
git clone <your-repo>
cd youtrack_messenger

# 2. Run setup script
./setup.sh

# 3. Edit configuration
# Edit .env file with your settings:
# - YOUTRACK_BASE_URL
# - YOUTRACK_TOKEN
# - TELEGRAM_BOT_TOKEN
# - TELEGRAM_PM_CHAT_ID
# - NGROK_AUTHTOKEN

# 4. Start services
./setup.sh
```

## 📋 Configuration

### Environment Variables (.env file)

```bash
# Port Configuration
APP_PORT=8080
NGROK_WEB_PORT=4040

# YouTrack Configuration
YOUTRACK_BASE_URL=https://your-instance.youtrack.cloud
YOUTRACK_TOKEN=your-youtrack-token

# Telegram Configuration  
TELEGRAM_BOT_TOKEN=your-bot-token
TELEGRAM_PM_CHAT_ID=your-chat-id

# ngrok Configuration
NGROK_AUTHTOKEN=your-ngrok-authtoken
```

## 🛠️ Management Commands

### Service Management
```bash
# Start services
./manage.sh start

# Stop services
./manage.sh stop

# Restart services
./manage.sh restart

# View logs
./manage.sh logs

# Check status
./manage.sh status
```

### Port Management
```bash
# Check port status
./configure-ports.sh show

# Change port configuration
./configure-ports.sh change
```

### Maintenance
```bash
# Update services
./manage.sh update

# Clean up Docker resources
./manage.sh clean
```

## 🌐 Access URLs

- **Bot Application**: http://localhost:8080
- **ngrok Web Interface**: http://localhost:4040

## 📊 Features

- **YouTrack Notifications**: Fetches notifications and sends to Telegram PM
- **Deduplication**: Prevents duplicate notifications using H2 database
- **Interactive Commands**: Create issues, list projects, manage scheduler
- **Automatic Webhook Setup**: ngrok integration with automatic Telegram webhook configuration
- **Health Monitoring**: Built-in health checks and monitoring

## 🔧 Architecture

### Docker Services
- **app**: Spring Boot application
- **ngrok**: Tunnel service for webhook
- **webhook-setup**: Automatic Telegram webhook configuration

### Data Persistence
- H2 database files stored in `./data/` directory
- Logs stored in `./logs/` directory
- Configuration via environment variables

## 🚨 Troubleshooting

### Port Conflicts
```bash
# Check for port conflicts
./port-check.sh

# Manually kill processes on ports
lsof -ti:8080 | xargs kill -9
lsof -ti:4040 | xargs kill -9
```

### Service Issues
```bash
# Check service status
docker-compose ps

# View detailed logs
docker-compose logs -f app

# Restart specific service
docker-compose restart app
```

### Clean Restart
```bash
# Stop all services
docker-compose down

# Remove containers and images
docker-compose down --rmi all

# Start fresh
./setup.sh
```

## 📝 Development

### Local Development
```bash
# Run tests
./gradlew test

# Build Docker image
docker build -t youtrack-messenger:latest .

# Run with Docker Compose
docker-compose up -d
```

### CI/CD
The project includes GitHub Actions CI that:
- Runs unit tests with `./gradlew test`
- Builds Docker image
- Tests Docker image functionality

## 🔄 Migration from Local Setup

If migrating from the local setup:

1. **Backup existing data** (optional, as Docker uses fresh database)
2. **Stop local services**
3. **Run Docker setup**: `./setup.sh`
4. **Configure environment**: Edit `.env` file
5. **Start services**: `./setup.sh`

## 📞 Support

For issues and questions:
1. Check the logs: `./manage.sh logs`
2. Verify configuration: `./manage.sh status`
3. Check port conflicts: `./configure-ports.sh show`
4. Clean restart: `./manage.sh clean && ./setup.sh`
