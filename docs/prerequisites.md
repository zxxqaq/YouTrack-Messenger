# 🤌 Prerequisites

## 1. YouTrack Configuration

### 1.1 YouTrack API Token
1. **Login to YouTrack**: Go to your YouTrack instance (e.g., `https://your-company.youtrack.cloud`)
2. **Access Profile Settings**: Click your avatar → Profile Settings
3. **Authentication Tab**: Navigate to "Authentication" tab
4. **Create New Token**:
    - Click "New token..."
    - Name: `telegram-bot-token`
    - Scope: Select appropriate permissions (read issues, create issues, read projects)
    - Click "Create"
5. **Copy Token**: Save the generated token securely

### 1.2 YouTrack Base URL
- Format: `https://your-company.youtrack.cloud`
- Example: `https://acme-corp.youtrack.cloud`

## 2. Telegram Configuration

### 2.1 Bot Token
1. **Create Bot**: Message [@BotFather](https://t.me/botfather) on Telegram
2. **Use /newbot**: Follow the prompts to create your bot
3. **Get Token**: Copy the token (format: `123456789:ABCdefGHIjklMNOpqrsTUVwxyz`)

### 2.2 Start your Bot

**Step 1: Open the Bot You Created**
- Method 1 (Recommended): Click the link directly (or copy and paste it into your browser)

   `https://t.me/YourBotUsername`
   
   Then Telegram will open your bot’s chat window. Click “Start” to begin chatting.

- Method 2: In Telegram’s search bar, enter the @BotUsername you set when creating the bot.
For example, if you set it as @hello_bot, just search for that.

**Step 2: Start the Bot (You MUST Click Start)**

The first time you use it, you need to click the “Start” button (or send /start) to activate the bot chat.


### 2.3 Your Chat ID

1. Message [@userinfobot](https://t.me/userinfobot) on Telegram
2. It will reply with your user ID


## 3. ngrok Configuration

1. **Sign up**: Create account at [ngrok.com](https://ngrok.com)
2. **Get Authtoken**: Go to [Dashboard → Your Authtoken](https://dashboard.ngrok.com/get-started/your-authtoken)
3. **Copy Token**: Save the authtoken for webhook setup
