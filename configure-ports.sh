#!/bin/bash
# configure-ports.sh - Port configuration management

show_current_ports() {
    echo "📋 Current port configuration:"
    echo "   App port: ${APP_PORT:-8080}"
    echo "   ngrok web port: ${NGROK_WEB_PORT:-4040}"
    echo ""
    
    echo "🔍 Port usage status:"
    for port in ${APP_PORT:-8080} ${NGROK_WEB_PORT:-4040}; do
        if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
            echo "   Port $port: ❌ In use"
            lsof -Pi :$port -sTCP:LISTEN | tail -n +2
        else
            echo "   Port $port: ✅ Available"
        fi
    done
}

change_ports() {
    echo "🔧 Port Configuration"
    echo "===================="
    
    # Read current configuration
    source .env 2>/dev/null || true
    current_app_port=${APP_PORT:-8080}
    current_ngrok_port=${NGROK_WEB_PORT:-4040}
    
    echo "Current configuration:"
    echo "  App port: $current_app_port"
    echo "  ngrok web port: $current_ngrok_port"
    echo ""
    
    # Get new ports
    read -p "Enter new app port [$current_app_port]: " new_app_port
    new_app_port=${new_app_port:-$current_app_port}
    
    read -p "Enter new ngrok web port [$current_ngrok_port]: " new_ngrok_port
    new_ngrok_port=${new_ngrok_port:-$current_ngrok_port}
    
    # Update .env file
    if [ -f .env ]; then
        sed -i.bak "s/APP_PORT=.*/APP_PORT=$new_app_port/" .env
        sed -i.bak "s/NGROK_WEB_PORT=.*/NGROK_WEB_PORT=$new_ngrok_port/" .env
        rm -f .env.bak
    fi
    
    echo "✅ Port configuration updated!"
    echo "   App port: $new_app_port"
    echo "   ngrok web port: $new_ngrok_port"
}

case "$1" in
    show)
        show_current_ports
        ;;
    change)
        change_ports
        ;;
    *)
        echo "Usage: $0 {show|change}"
        echo ""
        echo "Commands:"
        echo "  show   - Show current port configuration and usage"
        echo "  change - Change port configuration"
        exit 1
        ;;
esac
