// 📁 data/remote/websocket/WebSocketClient.kt
package com.example.elizarchat.data.remote.websocket

import android.util.Log
import okhttp3.*
import java.util.concurrent.TimeUnit

class WebSocketClient(
    private val token: String,
    private val baseUrl: String,
    private val onStateChanged: (WebSocketState) -> Unit,
    private val onMessageReceived: (String) -> Unit
) {
    companion object {
        private const val TAG = "WebSocketClient"
    }

    private var webSocket: WebSocket? = null
    private val okHttpClient = OkHttpClient.Builder()
        .pingInterval(30, TimeUnit.SECONDS) // Увеличили до 30 секунд
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS) // Увеличили read timeout
        .writeTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    fun connect() {
        try {
            val url = "$baseUrl?token=$token"
            Log.d(TAG, "🔗 Подключение к WebSocket: $url")

            val request = Request.Builder()
                .url(url)
                .addHeader("User-Agent", "ElizaChat-Android/1.0.0")
                .build()

            webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    Log.d(TAG, "✅ WebSocket соединение открыто")
                    onStateChanged(WebSocketState.Connected())
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    Log.d(TAG, "📨 Получено сообщение (${text.length} chars)")
                    if (text.length > 500) {
                        Log.d(TAG, "📨 Содержимое (первые 500): ${text.take(500)}...")
                    } else {
                        Log.d(TAG, "📨 Содержимое: $text")
                    }
                    onMessageReceived(text)
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    Log.d(TAG, "🔒 WebSocket соединение закрыто: $code $reason")
                    onStateChanged(WebSocketState.Disconnected)
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    Log.d(TAG, "🔒 WebSocket закрывается: $code $reason")
                    webSocket.close(1000, null)
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    Log.e(TAG, "💥 Ошибка WebSocket: ${t.message}", t)

                    // Проверяем тип ошибки
                    val errorMessage = when (t) {
                        is java.net.SocketTimeoutException -> "Connection timeout: ${t.message}"
                        is java.io.EOFException -> "Server closed connection unexpectedly"
                        else -> t.message ?: "Connection failed"
                    }

                    onStateChanged(WebSocketState.Error(errorMessage))
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "❌ Ошибка создания WebSocket соединения: ${e.message}")
            onStateChanged(WebSocketState.Error(e.message ?: "Unknown error"))
        }
    }

    fun disconnect() {
        Log.d(TAG, "🛑 Отключение WebSocket...")
        webSocket?.close(1000, "User disconnected")
        webSocket = null
        onStateChanged(WebSocketState.Disconnected)
    }

    fun sendMessage(message: String): Boolean {
        return try {
            val isSent = webSocket?.send(message) ?: false
            if (isSent) {
                Log.d(TAG, "📤 Сообщение отправлено (${message.length} chars)")
            } else {
                Log.w(TAG, "⚠️ Не удалось отправить сообщение")
            }
            isSent
        } catch (e: Exception) {
            Log.e(TAG, "❌ Ошибка отправки сообщения: ${e.message}")
            false
        }
    }
}