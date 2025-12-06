package com.example.elizarchat.data.remote.service

import android.util.Log
import com.example.elizarchat.AppConstants
import com.example.elizarchat.data.remote.ApiManager
import com.example.elizarchat.data.remote.dto.LoginRequestDto
import com.example.elizarchat.data.remote.dto.RegisterRequestDto
import com.example.elizarchat.data.remote.dto.websocket.ClientMessage
import com.example.elizarchat.data.remote.dto.websocket.ServerMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class ServerTestService {
    companion object {
        private const val TAG = "ServerTestService"
    }

    private val scope = CoroutineScope(Dispatchers.IO)

    fun runFullConnectionTest() {
        scope.launch {
            println("\n=== 🚀 ПОЛНЫЙ ТЕСТ ПОДКЛЮЧЕНИЯ К СЕРВЕРУ ===")
            println("Сервер: ${AppConstants.SERVER_BASE_URL}")

            // 1. Тест доступности сервера
            testServerAvailability()

            // 2. Тест REST API
            testRestApi()

            println("=== ТЕСТ ЗАВЕРШЕН ===\n")
        }
    }

    private suspend fun testServerAvailability() {
        println("\n1. 🔍 Проверка доступности сервера:")

        // Тест HTTPS API
        println("   - HTTPS API (REST):")
        testEndpoint("${AppConstants.SERVER_BASE_URL}/", "Root")
        testEndpoint(AppConstants.API_BASE_URL, "API Root")

        // Тест WebSocket Secure
        println("   - WebSocket Secure:")
        testWebSocketEndpoint(AppConstants.WS_BASE_URL)
    }

    private fun testEndpoint(url: String, name: String) {
        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()

        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("Accept", "application/json")
            .addHeader("User-Agent", AppConstants.USER_AGENT)
            .build()

        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                println("     ✅ $name: Доступен (${response.code})")
                val body = response.body?.string()?.take(100) ?: "Empty response"
                println("       Ответ: $body...")
            } else {
                println("     ⚠️ $name: Ошибка (${response.code})")
                println("       Сообщение: ${response.message}")
            }
            response.close()
        } catch (e: Exception) {
            println("     ❌ $name: Недоступен - ${e.message}")
        }
    }

    private fun testWebSocketEndpoint(url: String) {
        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .build()

        val request = Request.Builder()
            .url(url)
            .addHeader("User-Agent", AppConstants.USER_AGENT)
            .build()

        try {
            val webSocket = client.newWebSocket(request, object : okhttp3.WebSocketListener() {
                override fun onOpen(webSocket: okhttp3.WebSocket, response: okhttp3.Response) {
                    println("     ✅ WebSocket: Подключение успешно")
                    webSocket.close(1000, "Test completed")
                }

                override fun onFailure(webSocket: okhttp3.WebSocket, t: Throwable, response: okhttp3.Response?) {
                    println("     ❌ WebSocket: Ошибка подключения - ${t.message}")
                }
            })

            Thread.sleep(3000)

        } catch (e: Exception) {
            println("     ❌ WebSocket: Исключение - ${e.message}")
        }
    }

    private suspend fun testRestApi() {
        println("\n2. 🔧 Тест REST API:")

        // Создаем ApiManager без токена (для регистрации/входа)
        val apiManager = ApiManager()

        // Тест входа
        testLogin(apiManager)
    }

    private suspend fun testLogin(apiManager: ApiManager) {
        println("   - Вход пользователя:")

        val loginRequest = LoginRequestDto(
            username = AppConstants.TestCredentials.USERNAME,
            password = AppConstants.TestCredentials.PASSWORD
        )

        try {
            val response = apiManager.authApi.login(loginRequest)
            if (response.isSuccessful) {
                println("     ✅ Вход успешен!")
                val authResponse = response.body()
                println("       Токен получен: ${authResponse?.token?.take(20)}...")
                println("       Пользователь: ${authResponse?.user?.username}")
                println("       ID: ${authResponse?.user?.id}")
                println("       Email: ${authResponse?.user?.email}")

                // Тестируем другие API с полученным токеном
                authResponse?.token?.let { token ->
                    testAuthenticatedApis(token)
                }
            } else {
                println("     ❌ Вход не удался: ${response.code()}")
                val errorBody = response.errorBody()?.string()
                println("       Ошибка: $errorBody")

                // Пробуем разные варианты Content-Type
                testDifferentContentTypes()
            }
        } catch (e: Exception) {
            println("     ❌ Ошибка входа: ${e.message}")
            println("       Проверьте: ")
            println("       1. Сервер запущен на ${AppConstants.SERVER_BASE_URL}")
            println("       2. Пользователь ${AppConstants.TestCredentials.USERNAME} существует")
            println("       3. API путь правильный")
        }
    }

    private fun testDifferentContentTypes() {
        println("       Пробуем разные форматы запросов:")

        // Пробуем form-urlencoded
        println("       - application/x-www-form-urlencoded")
        // Можно добавить тест с другим Content-Type если нужно
    }

    private suspend fun testAuthenticatedApis(token: String) {
        println("\n3. 🔐 Тест аутентифицированных API:")

        val apiManager = ApiManager(token)

        // Тест получения текущего пользователя
        println("   - Получение текущего пользователя:")
        try {
            val response = apiManager.userApi.getCurrentUser()
            if (response.isSuccessful) {
                println("     ✅ Пользователь получен")
                val userResponse = response.body()
                val user = userResponse?.user

                if (user != null) {
                    println("       Username: ${user.username}")
                    println("       DisplayName: ${user.displayName}")
                    println("       Email: ${user.email ?: "Не указан"}")
                    println("       Online: ${user.isOnline}")
                    println("       Avatar: ${user.avatarUrl ?: "Нет аватара"}")
                    println("       Last seen: ${user.lastSeen}")
                    println("       Created: ${user.createdAt}")
                } else {
                    println("       ⚠️ Пользователь null в ответе")
                }
            } else {
                println("     ⚠️ Ошибка: ${response.code()}")
                println("       Сообщение: ${response.message()}")
            }
        } catch (e: Exception) {
            println("     ❌ Ошибка: ${e.message}")
            e.printStackTrace()
        }

        // Тест поиска пользователей
        println("   - Поиск пользователей:")
        try {
            val response = apiManager.userApi.searchUsers("test")
            if (response.isSuccessful) {
                val users = response.body()
                println("     ✅ Найдено пользователей: ${users?.size ?: 0}")
                users?.take(3)?.forEachIndexed { i, user ->
                    println("       ${i + 1}. ${user.username} (${user.displayName ?: "без имени"})")
                }
            } else {
                println("     ⚠️ Ошибка: ${response.code()}")
            }
        } catch (e: Exception) {
            println("     ❌ Ошибка: ${e.message}")
        }

        // Тест получения чатов
        println("   - Получение списка чатов:")
        try {
            val response = apiManager.chatApi.getChats()
            if (response.isSuccessful) {
                val chats = response.body()
                println("     ✅ Чатов получено: ${chats?.size ?: 0}")
                chats?.take(3)?.forEachIndexed { i, chat ->
                    println("       ${i + 1}. ${chat.name ?: "Без названия"} (${chat.type})")
                }
            } else {
                println("     ⚠️ Ошибка: ${response.code()}")
            }
        } catch (e: Exception) {
            println("     ❌ Ошибка: ${e.message}")
        }
    }

    private fun testWebSocketConnection(token: String) {
        println("\n4. 📡 Тест WebSocket подключения:")

        val webSocketService = WebSocketService(token)

        // Подписываемся на статус подключения
        scope.launch {
            webSocketService.connectionStatus.collect { isConnected ->
                if (isConnected) {
                    println("     ✅ WebSocket подключен")

                    // Тест отправки сообщений
                    Thread.sleep(1000)

                    // Подписываемся на чат (пример)
                    // webSocketService.sendMessage(ClientMessage.Subscribe(chatId = 1))

                    // Отправляем ping
                    webSocketService.sendMessage(ClientMessage.Ping)

                    // Отключаемся через 5 секунд
                    Thread.sleep(5000)
                    webSocketService.disconnect()
                    println("     🔌 WebSocket отключен")
                } else {
                    println("     🔌 WebSocket отключен")
                }
            }
        }

        // Подписываемся на сообщения
        scope.launch {
            webSocketService.messages.collect { message ->
                when (message) {
                    is ServerMessage.NewMessage -> {
                        println("     📩 Новое сообщение: ${message.message.content.take(50)}...")
                    }
                    is ServerMessage.Pong -> {
                        println("     🏓 Pong получен: ${message.timestamp}")
                    }
                    is ServerMessage.Error -> {
                        println("     ❌ WebSocket ошибка: ${message.code} - ${message.message}")
                    }
                    else -> {
                        println("     📨 Сообщение: ${message::class.simpleName}")
                    }
                }
            }
        }

        // Подключаемся
        webSocketService.connect()

        // Ждем подключения
        Thread.sleep(3000)
    }
}