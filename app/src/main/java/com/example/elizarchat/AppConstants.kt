package com.example.elizarchat

object AppConstants {
    const val DEBUG = true

    // Конфигурация сервера - ИЗМЕНИТЕ НА HTTPS
    const val SERVER_BASE_URL = "https://stalinvdote.ru"
    const val API_BASE_URL = "$SERVER_BASE_URL/api/v1/"
    const val WS_BASE_URL = "ws://stalinvdote.ru:3000" // WebSocket Secure

    // Конфигурация приложения
    const val APP_NAME = "ElizarChat"
    const val APP_VERSION = "1.0.0"
    const val USER_AGENT = "$APP_NAME-Android/$APP_VERSION"

    // Таймауты
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L

    // Тестовые данные (используем ваши данные)
    object TestCredentials {
        const val USERNAME = "kirill"
        const val PASSWORD = "edcrfv"
        const val EMAIL = "" // оставьте пустым если нет
    }
}