# YouTrack Messenger Bot

A Telegram bot that can pull notifications from YouTrack and allows users to create issues interactively.

## 🤖 Bot Introduction

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

# 5. Now you can communicate with your bot on Telegram
# See the <Bot-Intro> section for available commands and how to play with it

# 6. Manage services
./manage.sh stop     # stop services
./manage.sh start    # restart services
```

## 📁 Other info

- [project structure](docs/project-structure.md)
- [dependencies and services](docs/dependencies-services.md)
- [CI/CD pipeline](docs/CI.md)



