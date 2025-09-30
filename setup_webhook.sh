#!/bin/bash

# Telegram Bot Webhook Setup Script
BOT_TOKEN="7901869665:AAEPgDMkLT6g6kEqKZqc26Fwhdtc_xZksv4"
NGROK_AUTHTOKEN="33QCp5Ga1uOC9RaAtHvVbsaPAe2_3RBj9ZbojSeRLixQ1m4W8"

echo "🤖 Telegram Bot Webhook Setup"
echo "=============================="
echo ""

# Configure ngrok authtoken if needed
echo "🔧 Configuring ngrok authtoken..."
ngrok config add-authtoken "$NGROK_AUTHTOKEN" > /dev/null 2>&1
echo "✅ ngrok authtoken configured"
echo ""

# Check if ngrok is running
if ! curl -s http://127.0.0.1:4040/api/tunnels > /dev/null 2>&1; then
    echo "❌ ngrok is not running!"
    echo ""
    echo "Please start ngrok first:"
    echo "1. Open a new terminal window"
    echo "2. Run: ngrok http 8080"
    echo "3. Come back and run this script again"
    echo ""
    exit 1
fi

# Get ngrok URL
NGROK_URL=$(curl -s http://127.0.0.1:4040/api/tunnels | grep -o '"public_url":"[^"]*' | grep -o 'https://[^"]*' | head -1)

if [ -z "$NGROK_URL" ]; then
    echo "❌ Could not get ngrok URL. Make sure ngrok is running with: ngrok http 8080"
    exit 1
fi

WEBHOOK_URL="${NGROK_URL}/api/telegram/webhook"

echo "✅ Found ngrok URL: ${NGROK_URL}"
echo "🔗 Webhook URL: ${WEBHOOK_URL}"
echo ""

# Set webhook
echo "Setting up webhook..."
RESPONSE=$(curl -s -X POST "https://api.telegram.org/bot${BOT_TOKEN}/setWebhook" \
     -H "Content-Type: application/json" \
     -d "{\"url\": \"${WEBHOOK_URL}\"}")

echo "Response: ${RESPONSE}"
echo ""

if echo "$RESPONSE" | grep -q '"ok":true'; then
    echo "✅ Webhook setup successful!"
    echo ""
    echo "🎉 You can now test commands in Telegram:"
    echo "   /help - Show available commands"
    echo "   /create Test issue - Create a new issue"
    echo "   /projects - Show available projects"
    echo "   /status - Show bot status"
else
    echo "❌ Webhook setup failed!"
    echo "Response: ${RESPONSE}"
fi
