package com.example.elizarchat

object AppConstants {
    // Домен (используйте true когда домен будет настроен)
    const val USE_DOMAIN = false

    // Ваш домен
    const val SERVER_DOMAIN = "eliza-server.ru"

    // Fallback на IP (для разработки)
    const val SERVER_IP = "195.133.145.249"
    const val API_PORT = 3001
    const val WS_PORT = 3000

    // ========== СЕРВЕРНЫЕ URL ==========
    // Доменные URL (когда USE_DOMAIN = true)
    val API_BASE_URL_DOMAIN = "https://api.$SERVER_DOMAIN/api/v1"
    val WS_BASE_URL_DOMAIN = "wss://ws.$SERVER_DOMAIN"

    // IP URL (когда USE_DOMAIN = false или домен не настроен)
    val BASE_URL_IP = "http://$SERVER_IP:$API_PORT"
    val API_BASE_URL_IP = "$BASE_URL_IP/api/v1"
    val WS_BASE_URL_IP = "ws://$SERVER_IP:$WS_PORT"

    // Активные URL (автоматический выбор)
    val API_BASE_URL = if (USE_DOMAIN) API_BASE_URL_DOMAIN else API_BASE_URL_IP
    val WS_BASE_URL = if (USE_DOMAIN) WS_BASE_URL_DOMAIN else WS_BASE_URL_IP

    // Для отладки (показывает какой URL используется)
    val CURRENT_SERVER_CONFIG = if (USE_DOMAIN) "DOMAIN: $SERVER_DOMAIN" else "IP: $SERVER_IP"

    // ========== НАСТРОЙКИ ТОКЕНОВ ==========
    // Время жизни токенов (согласно серверу)
    const val ACCESS_TOKEN_LIFETIME_MINUTES = 15L
    const val REFRESH_TOKEN_LIFETIME_DAYS = 7L

    // Запас времени для обновления токена (за 2 минуты до истечения)
    const val TOKEN_REFRESH_THRESHOLD_MINUTES = 2L

    // ========== НАСТРОЙКИ ЗАПРОСОВ ==========
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L

    const val MAX_RETRY_COUNT = 3
    const val RETRY_DELAY_MS = 1000L

    // ========== КЛЮЧИ ХРАНИЛИЩА ==========
    const val PREFS_NAME = "eliza_chat"

    // Токены
    const val KEY_ACCESS_TOKEN = "access_token"
    const val KEY_REFRESH_TOKEN = "refresh_token"

    // Пользователь
    const val KEY_USER_ID = "user_id"
    const val KEY_USERNAME = "username"
    const val KEY_EMAIL = "email"
    const val KEY_DISPLAY_NAME = "display_name"
    const val KEY_AVATAR_URL = "avatar_url"

    // Время
    const val KEY_TOKEN_EXPIRY = "token_expiry"
    const val KEY_LAST_SYNC_TIME = "last_sync_time"
    const val KEY_LAST_LOGIN_TIME = "last_login_time"

    // Настройки
    const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    const val KEY_THEME_MODE = "theme_mode"
    const val KEY_LANGUAGE = "language"

    // ========== НАСТРОЙКИ БАЗЫ ДАННЫХ ==========
    const val DATABASE_NAME = "eliza_chat_db"
    const val DATABASE_VERSION = 1

    // ========== ПАГИНАЦИЯ И ЛИМИТЫ ==========
    const val DEFAULT_PAGE_SIZE = 20
    const val MAX_MESSAGE_LENGTH = 5000
    const val MAX_USERNAME_LENGTH = 50
    const val MAX_CHAT_NAME_LENGTH = 100

    // Лимиты
    const val MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024 // 10 MB
    const val MAX_IMAGE_DIMENSION = 1920

    // ========== ТИПЫ ЧАТОВ ==========
    object ChatType {
        const val PRIVATE = "private"
        const val GROUP = "group"
        const val CHANNEL = "channel"

        fun isValid(type: String): Boolean {
            return type == PRIVATE || type == GROUP || type == CHANNEL
        }
    }

    // ========== РОЛИ УЧАСТНИКОВ ==========
    object MemberRole {
        const val OWNER = "owner"
        const val ADMIN = "admin"
        const val MEMBER = "member"
        const val GUEST = "guest"

        fun canManageChat(role: String): Boolean {
            return role == OWNER || role == ADMIN
        }

        fun canSendMessages(role: String): Boolean {
            return role != GUEST
        }
    }

    // ========== СТАТУСЫ СООБЩЕНИЙ ==========
    object MessageStatus {
        const val SENDING = "sending"
        const val SENT = "sent"
        const val DELIVERED = "delivered"
        const val READ = "read"
        const val ERROR = "error"

        fun isFinal(status: String): Boolean {
            return status == SENT || status == DELIVERED || status == READ || status == ERROR
        }
    }

    // ========== ТИПЫ СООБЩЕНИЙ ==========
    object MessageType {
        const val TEXT = "text"
        const val IMAGE = "image"
        const val VIDEO = "video"
        const val FILE = "file"
        const val VOICE = "voice"
        const val SYSTEM = "system"
    }

    // ========== СОБЫТИЯ WEB SOCKET ==========
    object WebSocketEvent {
        const val MESSAGE_NEW = "message_new"
        const val MESSAGE_UPDATE = "message_update"
        const val MESSAGE_DELETE = "message_delete"
        const val TYPING_START = "typing_start"
        const val TYPING_STOP = "typing_stop"
        const val USER_ONLINE = "user_online"
        const val USER_OFFLINE = "user_offline"
        const val CHAT_UPDATE = "chat_update"
        const val CHAT_DELETE = "chat_delete"
    }

    // ========== КОДЫ ОШИБОК ==========
    object ErrorCode {
        const val NETWORK_ERROR = "network_error"
        const val SERVER_ERROR = "server_error"
        const val UNAUTHORIZED = "unauthorized"
        const val FORBIDDEN = "forbidden"
        const val NOT_FOUND = "not_found"
        const val VALIDATION_ERROR = "validation_error"
        const val TOKEN_EXPIRED = "token_expired"
        const val RATE_LIMIT = "rate_limit"
    }

    // ========== УТИЛИТНЫЕ МЕТОДЫ ==========

    /** Получить полный URL для API эндпоинта */
    fun getApiUrl(endpoint: String): String {
        return if (endpoint.startsWith("/")) {
            "$API_BASE_URL$endpoint"
        } else {
            "$API_BASE_URL/$endpoint"
        }
    }

    /** Получить URL для WebSocket с токеном */
    fun getWebSocketUrl(token: String): String {
        return if (USE_DOMAIN) {
            "$WS_BASE_URL/?token=$token"
        } else {
            "$WS_BASE_URL_IP/?token=$token"
        }
    }

    /** Проверить, включен ли HTTPS */
    fun isHttpsEnabled(): Boolean {
        return USE_DOMAIN || API_BASE_URL.startsWith("https://")
    }

    /** Получить сервер для логов */
    fun getServerInfo(): String {
        return """
            Server: ${if (USE_DOMAIN) "Domain ($SERVER_DOMAIN)" else "IP ($SERVER_IP)"}
            API: $API_BASE_URL
            WS: $WS_BASE_URL
            HTTPS: ${isHttpsEnabled()}
        """.trimIndent()
    }

    // ========== ФЛАГИ ОТЛАДКИ ==========
    object Debug {
        // Включить/выключить логирование
        const val LOG_NETWORK = true
        const val LOG_DATABASE = true
        const val LOG_WEBSOCKET = true

        // Симуляция задержки сети (мс)
        const val NETWORK_DELAY_MS = 0L

        // Симуляция ошибок (0 = никогда, 1 = всегда)
        const val SIMULATE_NETWORK_ERROR_RATE = 0f
    }
}