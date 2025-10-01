#!/bin/bash
# setup.sh - Smart one-click startup

set -e

echo "🚀 YouTrack Messenger Bot - Smart Setup"
echo "======================================"

# Check Docker
if ! command -v docker &> /dev/null; then
    echo "❌ Docker not found. Please install Docker first:"
    echo "   https://docs.docker.com/get-docker/"
    exit 1
fi

# Check Docker Compose
if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose not found. Please install Docker Compose first:"
    echo "   https://docs.docker.com/compose/install/"
    exit 1
fi

# Create environment file
if [ ! -f .env ]; then
    echo "📝 Creating .env file..."
    cp env.example .env
    
    echo "✅ .env file created!"
    echo "📝 Please edit .env file with your configuration:"
    echo "   - YouTrack URL and token"
    echo "   - Telegram bot token and chat ID"
    echo "   - ngrok authtoken"
    echo "   - Port numbers (if different from defaults)"
    echo ""
    echo "   Then run: ./setup.sh"
    exit 0
fi

# Load environment variables
source .env

# Port conflict detection
echo "🔍 Checking for port conflicts..."
chmod +x port-check.sh
if ! ./port-check.sh; then
    echo "❌ Port conflicts detected. Please resolve them first."
    exit 1
fi

# Create necessary directories
mkdir -p data logs

# Start all services
echo "🚀 Starting all services..."
docker-compose up -d --build

# Wait for services to start
echo "⏳ Waiting for services to start..."
sleep 45

# Check service status
echo "📊 Checking service status..."
docker-compose ps

# Display access information
echo ""
echo "🎉 Setup complete!"
echo "=================="
echo "📱 Bot is running on: http://localhost:${APP_PORT}"
echo "🌐 ngrok web interface: http://localhost:${NGROK_WEB_PORT}"
echo "📊 View logs: docker-compose logs -f app"
echo "🛑 Stop services: docker-compose down"
echo ""
echo "💡 The webhook will be automatically configured!"
echo "   Check the logs to see the webhook URL."
