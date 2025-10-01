#!/bin/bash
# manage.sh - Enhanced service management script

# Load environment variables
source .env 2>/dev/null || true
APP_PORT=${APP_PORT:-8080}
NGROK_WEB_PORT=${NGROK_WEB_PORT:-4040}

case "$1" in
    start)
        echo "🚀 Starting services..."
        docker-compose up -d
        ;;
    stop)
        echo "🛑 Stopping services..."
        docker-compose down
        ;;
    restart)
        echo "🔄 Restarting services..."
        docker-compose restart
        ;;
    logs)
        echo "📊 Showing logs..."
        docker-compose logs -f app
        ;;
    status)
        echo "📊 Service status..."
        docker-compose ps
        echo ""
        echo "🌐 Access URLs:"
        echo "   Bot: http://localhost:$APP_PORT"
        echo "   ngrok: http://localhost:$NGROK_WEB_PORT"
        ;;
    ports)
        echo "🔍 Port status..."
        ./configure-ports.sh show
        ;;
    update)
        echo "🔄 Updating services..."
        git pull
        docker-compose up -d --build
        ;;
    clean)
        echo "🧹 Cleaning up..."
        docker-compose down
        docker system prune -f
        echo "✅ Cleanup complete"
        ;;
    *)
        echo "Usage: $0 {start|stop|restart|logs|status|ports|update|clean}"
        echo ""
        echo "Commands:"
        echo "  start   - Start all services"
        echo "  stop    - Stop all services"
        echo "  restart - Restart all services"
        echo "  logs    - Show application logs"
        echo "  status  - Show service status and URLs"
        echo "  ports   - Show port configuration and usage"
        echo "  update  - Update and restart services"
        echo "  clean   - Clean up Docker resources"
        exit 1
        ;;
esac
