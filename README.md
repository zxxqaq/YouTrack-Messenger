# YouTrack Messenger Bot

A Telegram bot that can pull notifications from YouTrack and allows users to create issues interactively.

## 🤖 Bot Introduction

This Telegram bot integrates with YouTrack to provide:

**Automatic Notifications**
- Configurable polling interval (default: 30s in Docker, 10s locally)
- Configurable notification count per fetch (default: 1000)
- Sends them directly to your Telegram private messages
- Prevents duplicate notifications with persistent storage

*Configuration files: `src/main/resources/application.yml` (local) or `docker/app/application-docker.yml` (Docker)*

**Interactive Commands**
- `/start` - Show welcome message and available commands
- `/status` - Display system health and polling status
- `/projects` - List all available YouTrack projects
- `/create <summary> @<project_id>` - Create new issues in YouTrack
- `/pull` - Start notification polling
- `/stop` - Stop notification polling

**Key Features**
- Real-time YouTrack integration with deduplication
- Configurable pagination (page size, delay between messages)
- Persistent H2 database for data storage
- Automatic webhook setup via ngrok
- Health monitoring and error reporting
- GitHub Actions CI pipeline with automated testing
- Docker containerization for easy deployment


**Configuration Examples:**
```yaml
scheduler:
  fixed-delay: PT30S        # Polling interval (30 seconds)
  top: 1000                 # Max notifications per fetch
  pagination:
    enabled: true
    page-size: 1            # Messages per page
    delay-between-messages: PT1S  # Delay between messages
```


## 🤌 Prerequisites

You need to preprare your tokens and ids before start running this application. Please refer to the instructions in [prerequisites.md](docs/prerequisites.md)

## 🚀 Quick Start

For detailed troubleshooting and service management, you can check [docker.md](docs/docker.md)

```bash
# 1. Clone the repository
git clone <your-repo-url>
cd youtrack_messenger

# 2. Check docker has been installed
docker --version 
docker-compose --version

# 3. Config .env file, copy the example and fill in your tokens and IDs
# How to get these tokens and IDs is explained in the <Configuration> section below
cp .env.example .env

# 4. Run one-click setup
./setup.sh # first time setup

# 5. Now you can communicate with your bot on Telegram, type /start to begin
# See the <Bot-Intro> section for available commands and how to play with it

# 6. Manage services
./manage.sh stop     # stop services
./manage.sh start    # restart services
```

## 📁 Other info

- [project structure](docs/project-structure.md)
- [dependencies and services](docs/dependencies-services.md)
- [CI/CD pipeline](docs/CI.md)



