package com.example.elizarchat.data.remote.service

import android.util.Log
import com.example.elizarchat.data.remote.dto.websocket.ClientMessage
import com.example.elizarchat.data.remote.dto.websocket.ServerMessage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit
import com.example.elizarchat.AppConstants


class WebSocketService(
    private val token: String
) {
    companion object {
        private const val TAG = "WebSocketService"
        // Используем константу из AppConstants
        private const val WS_URL = AppConstants.WS_BASE_URL
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    private var webSocket: WebSocket? = null
    private var isConnected = false
    private var reconnectAttempts = 0
    private val maxReconnectAttempts = 5

    private val _messages = MutableSharedFlow<ServerMessage>(replay = 0)
    val messages = _messages.asSharedFlow()

    private val _connectionStatus = MutableSharedFlow<Boolean>(replay = 1)
    val connectionStatus = _connectionStatus.asSharedFlow()

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .pingInterval(20, TimeUnit.SECONDS)
        .build()

    fun connect() {
        Log.d(TAG, "Connecting to WebSocket: $WS_URL")

        val request = Request.Builder()
            .url("$WS_URL?token=$token")
            .addHeader("User-Agent", "ElizarChat-Android/1.0")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "✅ WebSocket connected successfully")
                isConnected = true
                reconnectAttempts = 0
                _connectionStatus.tryEmit(true)

                // Отправляем начальный ping
                sendPing()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(TAG, "📩 Received: ${text.take(200)}...")
                try {
                    // Пытаемся определить тип сообщения
                    val message = parseWebSocketMessage(text)
                    if (message != null) {
                        _messages.tryEmit(message)
                    } else {
                        Log.w(TAG, "Unknown message format: $text")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing WebSocket message", e)
                    Log.e(TAG, "Raw message: $text")
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "❌ WebSocket failure: ${t.message}", t)
                isConnected = false
                _connectionStatus.tryEmit(false)

                if (reconnectAttempts < maxReconnectAttempts) {
                    reconnectAttempts++
                    Log.d(TAG, "Reconnecting... Attempt $reconnectAttempts")
                    Thread.sleep(2000L * reconnectAttempts)
                    reconnect()
                } else {
                    Log.e(TAG, "Max reconnection attempts reached")
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closed: $code - $reason")
                isConnected = false
                _connectionStatus.tryEmit(false)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closing: $code - $reason")
            }
        })
    }

    private fun parseWebSocketMessage(text: String): ServerMessage? {
        return try {
            // Простой парсинг по типу
            val type = extractMessageType(text)

            when (type) {
                "new_message" -> json.decodeFromString<ServerMessage.NewMessage>(text)
                "message_status" -> json.decodeFromString<ServerMessage.MessageStatus>(text)
                "user_status" -> json.decodeFromString<ServerMessage.UserStatus>(text)
                "new_chat" -> json.decodeFromString<ServerMessage.NewChat>(text)
                "notification" -> json.decodeFromString<ServerMessage.Notification>(text)
                "error" -> json.decodeFromString<ServerMessage.Error>(text)
                "pong" -> json.decodeFromString<ServerMessage.Pong>(text)
                else -> {
                    Log.w(TAG, "Unknown message type: $type")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse message: $text", e)
            null
        }
    }

    private fun extractMessageType(jsonString: String): String? {
        return try {
            // Простой поиск поля "type" в JSON
            val typePattern = "\"type\"\\s*:\\s*\"([^\"]+)\"".toRegex()
            val match = typePattern.find(jsonString)
            match?.groupValues?.get(1)
        } catch (e: Exception) {
            null
        }
    }

    fun disconnect() {
        Log.d(TAG, "Disconnecting WebSocket...")
        webSocket?.close(1000, "Normal closure")
        webSocket = null
        isConnected = false
        _connectionStatus.tryEmit(false)
    }

    fun sendMessage(message: ClientMessage) {
        if (!isConnected) {
            Log.w(TAG, "Cannot send message: WebSocket not connected")
            return
        }

        try {
            val jsonString = when (message) {
                ClientMessage.Ping -> """{"type":"ping"}"""
                is ClientMessage.Subscribe -> {
                    // Ручная сериализация для простоты
                    """{"type":"subscribe","data":{"chatId":${message.chatId}}}"""
                }
                is ClientMessage.SendMessage -> {
                    // Экранируем кавычки в content
                    val escapedContent = message.content.replace("\"", "\\\"")
                    """{"type":"send_message","data":{"chatId":${message.chatId},"content":"$escapedContent","type":"${message.type}"}}"""
                }
                is ClientMessage.MarkRead -> {
                    val messageIds = message.messageIds.joinToString(",")
                    """{"type":"mark_read","data":{"chatId":${message.chatId},"messageIds":[$messageIds]}}"""
                }
            }

            webSocket?.send(jsonString)
            Log.d(TAG, "📤 Sent: ${jsonString.take(200)}...")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending WebSocket message", e)
        }
    }

    private fun sendPing() {
        if (isConnected) {
            webSocket?.send("""{"type":"ping"}""")
        }
    }

    fun isConnected(): Boolean = isConnected

    private fun reconnect() {
        disconnect()
        connect()
    }
}