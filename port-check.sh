#!/bin/bash
# port-check.sh - Port conflict detection and cleanup

check_and_free_port() {
    local port=$1
    local service_name=$2
    
    echo "🔍 Checking port $port for $service_name..."
    
    # Check if port is occupied
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
        echo "⚠️  Port $port is already in use by:"
        lsof -Pi :$port -sTCP:LISTEN
        
        echo ""
        read -p "Do you want to kill the process using port $port? (y/N): " -n 1 -r
        echo ""
        
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            echo "🛑 Killing processes on port $port..."
            lsof -ti:$port | xargs kill -9 2>/dev/null || true
            sleep 2
            
            # Check again
            if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
                echo "❌ Failed to free port $port"
                return 1
            else
                echo "✅ Port $port is now free"
                return 0
            fi
        else
            echo "❌ Port $port is still in use. Please free it manually."
            return 1
        fi
    else
        echo "✅ Port $port is available"
        return 0
    fi
}

# Check Docker containers occupying ports
check_docker_ports() {
    echo "🐳 Checking for existing Docker containers..."
    
    # Check if there are containers with the same name
    if docker ps -a --format "table {{.Names}}" | grep -q "youtrack-messenger"; then
        echo "⚠️  Found existing YouTrack Messenger containers"
        docker ps -a --filter "name=youtrack-messenger"
        
        echo ""
        read -p "Do you want to remove existing containers? (y/N): " -n 1 -r
        echo ""
        
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            echo "🛑 Removing existing containers..."
            docker-compose down 2>/dev/null || true
            docker container prune -f
            echo "✅ Existing containers removed"
        fi
    fi
}

# Main function
main() {
    echo "🔍 Port Conflict Detection and Resolution"
    echo "========================================"
    
    # Read port configuration
    source .env 2>/dev/null || true
    APP_PORT=${APP_PORT:-8080}
    NGROK_WEB_PORT=${NGROK_WEB_PORT:-4040}
    
    echo "📋 Checking ports:"
    echo "   App port: $APP_PORT"
    echo "   ngrok web port: $NGROK_WEB_PORT"
    echo ""
    
    # Check Docker containers
    check_docker_ports
    echo ""
    
    # Check ports
    local all_ports_free=true
    
    if ! check_and_free_port $APP_PORT "Application"; then
        all_ports_free=false
    fi
    echo ""
    
    if ! check_and_free_port $NGROK_WEB_PORT "ngrok Web Interface"; then
        all_ports_free=false
    fi
    echo ""
    
    if [ "$all_ports_free" = true ]; then
        echo "✅ All ports are free! Ready to start services."
        return 0
    else
        echo "❌ Some ports are still in use. Please resolve conflicts manually."
        return 1
    fi
}

main "$@"
