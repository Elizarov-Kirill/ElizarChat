// 📁 data/remote/websocket/WebSocketManager.kt
package com.example.elizarchat.data.remote.websocket

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.elizarchat.AppConstants
import com.example.elizarchat.data.local.RefreshManager
import com.example.elizarchat.data.local.session.TokenManager
import com.example.elizarchat.data.remote.ApiManager
import com.example.elizarchat.data.remote.dto.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Clock

class WebSocketManager(
    private val context: Context,
    private val tokenManager: TokenManager,
    private val apiManager: ApiManager
) : LifecycleEventObserver {

    companion object {
        private const val TAG = "WebSocketManager"
        private val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            classDiscriminator = "type"
            serializersModule = SerializersModule {
                // Регистрируем все типы входящих сообщений
                polymorphic(WebSocketIncomingMessage::class) {
                    subclass(PingMessage::class)
                    subclass(TypingMessage::class)
                    subclass(SendMessageRequest::class)
                    subclass(SubscribeChatMessage::class)
                    subclass(UnsubscribeChatMessage::class)
                    subclass(ReadReceiptMessage::class)
                }
                // Регистрируем все типы исходящих сообщений
                polymorphic(WebSocketOutgoingMessage::class) {
                    subclass(WelcomeMessage::class)
                    subclass(NewMessageEvent::class)
                    subclass(UserTypingEvent::class)
                    subclass(MessageSentConfirmation::class)
                    subclass(ChatSubscribed::class)
                    subclass(ChatUnsubscribed::class)
                    subclass(ReadReceiptAck::class)
                    subclass(PongMessage::class)
                    subclass(ErrorMessage::class)
                    subclass(SystemMessage::class)
                    subclass(UserStatusUpdate::class)
                    subclass(ChatUpdate::class)
                }
            }
        }
        private const val RECONNECT_DELAY_MS = 5000L
        private const val MAX_RECONNECT_ATTEMPTS = 10
        private const val PING_INTERVAL_MS = 20000L // 20 секунд
    }

    private val isConnecting = AtomicBoolean(false)
    private var webSocketClient: WebSocketClient? = null
    private var reconnectJob: Job? = null
    private var pingJob: Job? = null
    private var reconnectAttempts = 0
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Потоки состояний
    private val _connectionState = MutableStateFlow<WebSocketState>(WebSocketState.Disconnected)
    val connectionState: StateFlow<WebSocketState> = _connectionState.asStateFlow()

    // Потоки событий
    private val _incomingMessages = MutableSharedFlow<WebSocketOutgoingMessage>()
    val incomingMessages: SharedFlow<WebSocketOutgoingMessage> = _incomingMessages.asSharedFlow()

    private val _newMessages = MutableSharedFlow<NewMessageEvent>()
    val newMessages: SharedFlow<NewMessageEvent> = _newMessages.asSharedFlow()

    private val _typingEvents = MutableSharedFlow<UserTypingEvent>()
    val typingEvents: SharedFlow<UserTypingEvent> = _typingEvents.asSharedFlow()

    private val _messageConfirmations = MutableSharedFlow<MessageSentConfirmation>()
    val messageConfirmations: SharedFlow<MessageSentConfirmation> = _messageConfirmations.asSharedFlow()

    private val _chatSubscribed = MutableSharedFlow<ChatSubscribed>()
    val chatSubscribed: SharedFlow<ChatSubscribed> = _chatSubscribed.asSharedFlow()

    private val _chatUnsubscribed = MutableSharedFlow<ChatUnsubscribed>()
    val chatUnsubscribed: SharedFlow<ChatUnsubscribed> = _chatUnsubscribed.asSharedFlow()

    private val _readReceipts = MutableSharedFlow<ReadReceiptAck>()
    val readReceipts: SharedFlow<ReadReceiptAck> = _readReceipts.asSharedFlow()

    private val _errors = MutableSharedFlow<ErrorMessage>()
    val errors: SharedFlow<ErrorMessage> = _errors.asSharedFlow()

    private val _welcomeMessages = MutableSharedFlow<WelcomeMessage>()
    val welcomeMessages: SharedFlow<WelcomeMessage> = _welcomeMessages.asSharedFlow()

    private val _userStatusUpdates = MutableSharedFlow<UserStatusUpdate>()
    val userStatusUpdates: SharedFlow<UserStatusUpdate> = _userStatusUpdates.asSharedFlow()

    private val _chatUpdates = MutableSharedFlow<ChatUpdate>()
    val chatUpdates: SharedFlow<ChatUpdate> = _chatUpdates.asSharedFlow()

    private val _systemMessages = MutableSharedFlow<SystemMessage>()
    val systemMessages: SharedFlow<SystemMessage> = _systemMessages.asSharedFlow()

    private val _pongMessages = MutableSharedFlow<PongMessage>()
    val pongMessages: SharedFlow<PongMessage> = _pongMessages.asSharedFlow()

    // Функция для запуска подключения
    fun connect() {
        if (isConnecting.getAndSet(true)) {
            println("⚠️ WebSocket уже подключается...")
            return
        }

        reconnectAttempts = 0
        performConnect()
    }

    private fun performConnect() {
        scope.launch {
            try {
                // 1. Сначала получаем или обновляем токен
                var token = tokenManager.getAccessToken()

                // Проверяем истек ли токен
                if (tokenManager.isAccessTokenExpired()) {
                    println("🔄 Access токен истек, пытаемся обновить...")
                    val refreshSuccess = apiManager.refreshAccessToken()
                    if (refreshSuccess) {
                        token = tokenManager.getAccessToken()
                        println("✅ Токен обновлен: ${token?.take(20)}...")
                    } else {
                        println("❌ Не удалось обновить токен")
                        _connectionState.value = WebSocketState.Error("Token refresh failed")
                        isConnecting.set(false)
                        return@launch
                    }
                }

                if (token.isNullOrEmpty()) {
                    println("❌ Нет access токена для WebSocket")
                    _connectionState.value = WebSocketState.Error("No access token")
                    isConnecting.set(false)
                    return@launch
                }

                println("🚀 Начинаем подключение WebSocket с токеном: ${token.take(20)}...")
                _connectionState.value = WebSocketState.Connecting

                val url = "${AppConstants.WS_BASE_URL}?token=$token"
                println("🔗 Подключение к WebSocket: $url")

                // Создаем WebSocket клиент
                webSocketClient = WebSocketClient(
                    token = token,
                    baseUrl = AppConstants.WS_BASE_URL,
                    onStateChanged = { state ->
                        handleStateChange(state)
                    },
                    onMessageReceived = { message ->
                        handleIncomingMessage(message)
                    }
                )

                webSocketClient?.connect()
            } catch (e: Exception) {
                println("💥 Исключение при подключении WebSocket: ${e.message}")
                handleStateChange(WebSocketState.Error("Connection failed: ${e.message}"))
            }
        }
    }

    private fun handleStateChange(state: WebSocketState) {
        println("🔄 Состояние WebSocket изменилось: $state")
        _connectionState.value = state

        when (state) {
            is WebSocketState.Connected -> {
                isConnecting.set(false)
                reconnectAttempts = 0
                startPingTask()
                println("✅ WebSocket успешно подключен")
            }
            is WebSocketState.Error -> {
                isConnecting.set(false)
                scheduleReconnect()
                println("❌ WebSocket ошибка: ${state.message}")
            }
            is WebSocketState.Disconnected -> {
                isConnecting.set(false)
                scheduleReconnect()
                println("🔌 WebSocket отключен")
            }
            else -> { /* Connecting */ }
        }
    }

    fun disconnect() {
        println("🛑 Принудительное отключение WebSocket...")
        reconnectJob?.cancel()
        pingJob?.cancel()
        webSocketClient?.disconnect()
        webSocketClient = null
        _connectionState.value = WebSocketState.Disconnected
        isConnecting.set(false)
        reconnectAttempts = 0
    }

    // Отправка сообщений с правильной сериализацией
    fun sendMessage(message: WebSocketIncomingMessage): Boolean {
        return try {
            val jsonString = WebSocketMessageHelper.serializeIncomingMessage(message)
            println("📤 Отправка WebSocket: type=${message}, data=${jsonString.take(200)}...")
            webSocketClient?.sendMessage(jsonString) ?: false
        } catch (e: Exception) {
            println("❌ Ошибка сериализации сообщения: ${e.message}")
            false
        }
    }

    // Отправка сообщения в чат
    fun sendChatMessage(chatId: Int, content: String, replyTo: Int? = null): Boolean {
        println("📤 Отправка сообщения через WebSocket: chatId=$chatId, content='${content.take(50)}...'")

        // ИСПРАВЛЕНО: Добавлены все обязательные поля
        val message = SendMessageRequest(
            chatId = chatId,
            content = content,
            messageType = "text",  // Обязательное поле с дефолтным значением
            replyTo = replyTo,
            metadata = "{}"        // Обязательное поле с дефолтным значением
        )

        return sendMessage(message)
    }

    // Отправка статуса печатания
    fun sendTypingStatus(chatId: Int, isTyping: Boolean): Boolean {
        val message = TypingMessage(
            chatId = chatId,
            isTyping = isTyping
        )
        return sendMessage(message)
    }

    // Отправка подтверждения прочтения
    fun sendReadReceipt(chatId: Int, messageIds: List<Int>): Boolean {
        println("👁️ Отправка read receipt для сообщений: $messageIds")
        val message = ReadReceiptMessage(
            chatId = chatId,
            messageIds = messageIds
        )
        return sendMessage(message)
    }

    // Подписка на чат
    fun subscribeToChat(chatId: Int): Boolean {
        println("➕ Подписка на чат: $chatId")
        val message = SubscribeChatMessage(chatId = chatId)
        return sendMessage(message)
    }

    // Отписка от чата
    fun unsubscribeFromChat(chatId: Int): Boolean {
        println("➖ Отписка от чата: $chatId")
        val message = UnsubscribeChatMessage(chatId = chatId)
        return sendMessage(message)
    }

    // Обработка входящих сообщений
    private fun handleIncomingMessage(jsonString: String) {
        try {
            val message = WebSocketMessageHelper.deserializeOutgoingMessage(jsonString)

            if (message == null) {
                println("⚠️ Не удалось десериализовать сообщение")
                return
            }

            when (message) {
                is WelcomeMessage -> {
                    println("🎉 Получено welcome сообщение")
                    scope.launch { _welcomeMessages.emit(message) }
                }

                is NewMessageEvent -> {
                    val effectiveChatId = message.getEffectiveChatId()
                    println("📨 Получено новое сообщение в чате $effectiveChatId")

                    scope.launch {
                        _newMessages.emit(message)
                        RefreshManager.notifyChatsChanged()
                        println("🔄 Список чатов обновлен (новое сообщение)")
                    }
                }

                is UserTypingEvent -> {
                    println("⌨️ Пользователь печатает")
                    scope.launch { _typingEvents.emit(message) }
                }

                is MessageSentConfirmation -> {
                    println("✅ Подтверждение отправки")
                    scope.launch { _messageConfirmations.emit(message) }
                }

                is ChatSubscribed -> {
                    println("✅ Подписка на чат: ${message.chatId}")
                    scope.launch { _chatSubscribed.emit(message) }
                }

                is ChatUnsubscribed -> {
                    println("✅ Отписка от чата: ${message.chatId}")
                    scope.launch { _chatUnsubscribed.emit(message) }
                }

                is ReadReceiptAck -> {
                    println("👁️ Подтверждение прочтения")
                    scope.launch { _readReceipts.emit(message) }
                }

                is ErrorMessage -> {
                    println("❌ Ошибка: ${message.message}")
                    scope.launch { _errors.emit(message) }
                }

                is PongMessage -> {
                    println("❤️ Получен pong")
                    scope.launch { _pongMessages.emit(message) }
                }

                is SystemMessage -> {
                    println("ℹ️ Системное сообщение: ${message.message}")
                    scope.launch { _systemMessages.emit(message) }
                }

                is UserStatusUpdate -> {
                    println("👤 Статус пользователя: ${message.userId}")
                    scope.launch { _userStatusUpdates.emit(message) }
                }

                is ChatUpdate -> {
                    println("💬 Обновление чата: chatId=${message.chatId}, action=${message.action}")

                    // 🔥 ВЕСЬ КОД ВНУТРИ ОДНОЙ КОРУТИНЫ
                    scope.launch {
                        _chatUpdates.emit(message)

                        if (message.action == "update" || message.action == "delete") {
                            try {
                                RefreshManager.notifyChatsChanged()
                                println("🔄 Список чатов обновлен (ChatUpdate: ${message.action})")
                            } catch (e: Exception) {
                                println("❌ Ошибка уведомления: ${e.message}")
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("💥 Ошибка обработки сообщения: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun scheduleReconnect() {
        if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
            println("❌ Достигнуто максимальное количество попыток переподключения ($MAX_RECONNECT_ATTEMPTS)")
            return
        }

        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            val delayMs = RECONNECT_DELAY_MS * (1 shl reconnectAttempts).coerceAtMost(60) // Exponential backoff
            println("🔄 Планируем переподключение через ${delayMs/1000} секунд... (попытка ${reconnectAttempts + 1})")

            delay(delayMs)

            if (connectionState.value !is WebSocketState.Connected &&
                connectionState.value !is WebSocketState.Connecting) {
                reconnectAttempts++
                performConnect()
            }
        }
    }

    private fun startPingTask() {
        pingJob?.cancel()
        pingJob = scope.launch {
            while (true) {
                delay(PING_INTERVAL_MS)
                if (connectionState.value is WebSocketState.Connected) {
                    sendPing()
                }
            }
        }
    }

    private fun sendPing() {
        try {
            val message = PingMessage(timestamp = Clock.System.now().toString())
            val jsonString = WebSocketMessageHelper.serializeIncomingMessage(message)
            webSocketClient?.sendMessage(jsonString)
            println("🏓 Отправлен ping")
        } catch (e: Exception) {
            println("❌ Ошибка отправки ping: ${e.message}")
        }
    }

    // Lifecycle API
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                println("📱 Приложение возобновило работу, подключаем WebSocket...")
                if (connectionState.value is WebSocketState.Disconnected) {
                    connect()
                }
            }
            Lifecycle.Event.ON_PAUSE -> {
                println("📱 Приложение на паузе...")
                // Не отключаем, но приостанавливаем ping
                pingJob?.cancel()
            }
            Lifecycle.Event.ON_STOP -> {
                println("📱 Приложение остановлено, отключаем WebSocket...")
                // Отключаем WebSocket когда приложение в фоне
                disconnect()
            }
            else -> { /* Другие события не обрабатываем */ }
        }
    }

    fun isConnected(): Boolean {
        return connectionState.value is WebSocketState.Connected
    }
}