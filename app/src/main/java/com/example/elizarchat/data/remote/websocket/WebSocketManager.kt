package com.example.elizarchat.data.remote.websocket

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.elizarchat.AppConstants
import com.example.elizarchat.data.local.session.TokenManager
import com.example.elizarchat.data.remote.ApiManager
import com.example.elizarchat.data.remote.dto.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import java.util.concurrent.atomic.AtomicBoolean

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
        private const val PING_INTERVAL_MS = 25000L
    }

    private val isConnecting = AtomicBoolean(false)
    private var webSocketClient: WebSocketClient? = null
    private var reconnectJob: Job? = null
    private var pingJob: Job? = null

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

    // Функция для запуска подключения
    fun connect() {
        if (isConnecting.getAndSet(true)) {
            println("⚠️ WebSocket уже подключается...")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
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
                        println("🔄 Состояние WebSocket изменилось: $state")
                        _connectionState.value = state
                        when (state) {
                            is WebSocketState.Connected -> {
                                isConnecting.set(false)
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
                            else -> {
                                // Для полноты when, хотя других состояний нет
                            }
                        }
                    },
                    onMessageReceived = { message ->
                        handleIncomingMessage(message)
                    }
                )

                webSocketClient?.connect()
            } catch (e: Exception) {
                println("💥 Исключение при подключении WebSocket: ${e.message}")
                _connectionState.value = WebSocketState.Error("Connection failed: ${e.message}")
                isConnecting.set(false)
                scheduleReconnect()
            }
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
    }

    // Отправка сообщений с правильной сериализацией
    fun sendMessage(message: WebSocketIncomingMessage): Boolean {
        return try {
            val jsonString = json.encodeToString(
                kotlinx.serialization.PolymorphicSerializer(WebSocketIncomingMessage::class),
                message
            )
            println("📤 Отправка WebSocket: ${jsonString.take(200)}...")
            webSocketClient?.sendMessage(jsonString) ?: false
        } catch (e: Exception) {
            println("❌ Ошибка сериализации сообщения: ${e.message}")
            false
        }
    }

    // Отправка сообщения в чат
    fun sendChatMessage(chatId: Int, content: String, replyTo: Int? = null): Boolean {
        println("📤 Отправка сообщения через WebSocket: chatId=$chatId, content='${content.take(50)}...'")
        val message = SendMessageRequest(
            chatId = chatId,
            content = content,
            messageType = "text",
            replyTo = replyTo,
            metadata = "{}" // Пустой JSON объект
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
            // Используем наш helper для определения типа и десериализации
            val message = WebSocketMessageHelper.deserializeOutgoingMessage(jsonString)

            if (message == null) {
                println("⚠️ Не удалось десериализовать сообщение")
                println("📝 Сырое сообщение: ${jsonString.take(500)}...")
                return
            }

            // Отправляем в общий поток
            CoroutineScope(Dispatchers.Main).launch {
                _incomingMessages.emit(message)
            }

            // Обрабатываем конкретные типы сообщений
            when (message) {
                is WelcomeMessage -> {
                    println("🎉 Получено welcome сообщение")
                    CoroutineScope(Dispatchers.Main).launch {
                        _welcomeMessages.emit(message)
                    }
                }

                is NewMessageEvent -> {
                    println("📨 Получено новое сообщение через WebSocket")
                    CoroutineScope(Dispatchers.Main).launch {
                        _newMessages.emit(message)
                    }
                }

                is UserTypingEvent -> {
                    println("⌨️ Пользователь печатает: userId=${message.userId}, chatId=${message.chatId}")
                    CoroutineScope(Dispatchers.Main).launch {
                        _typingEvents.emit(message)
                    }
                }

                is MessageSentConfirmation -> {
                    println("✅ Подтверждение отправки сообщения: messageId=${message.messageId}")
                    CoroutineScope(Dispatchers.Main).launch {
                        _messageConfirmations.emit(message)
                    }
                }

                is ReadReceiptAck -> {
                    println("👁️ Подтверждение прочтения: messageIds=${message.messageIds}")
                    CoroutineScope(Dispatchers.Main).launch {
                        _readReceipts.emit(message)
                    }
                }

                is ErrorMessage -> {
                    println("❌ Ошибка WebSocket: ${message.message}")
                    CoroutineScope(Dispatchers.Main).launch {
                        _errors.emit(message)
                    }
                }

                is PongMessage -> {
                    println("❤️ Получен pong от сервера: ${message.timestamp}")
                }

                is SystemMessage -> {
                    println("ℹ️ Системное сообщение: ${message.message}")
                    CoroutineScope(Dispatchers.Main).launch {
                        _systemMessages.emit(message)
                    }
                }

                is UserStatusUpdate -> {
                    println("👤 Обновление статуса пользователя: userId=${message.userId}, isOnline=${message.isOnline}")
                    CoroutineScope(Dispatchers.Main).launch {
                        _userStatusUpdates.emit(message)
                    }
                }

                is ChatUpdate -> {
                    println("💬 Обновление чата: chatId=${message.chatId}, action=${message.action}")
                    CoroutineScope(Dispatchers.Main).launch {
                        _chatUpdates.emit(message)
                    }
                }
            }
        } catch (e: Exception) {
            println("💥 Ошибка обработки WebSocket сообщения: ${e.message}")
            println("📝 Сырое сообщение: ${jsonString.take(500)}...")
        }
    }

    private fun scheduleReconnect() {
        reconnectJob?.cancel()
        reconnectJob = CoroutineScope(Dispatchers.IO).launch {
            delay(RECONNECT_DELAY_MS)

            if (connectionState.value !is WebSocketState.Connected &&
                connectionState.value !is WebSocketState.Connecting) {
                println("🔄 Планируем переподключение через 5 секунд...")
                connect()
            }
        }
    }

    private fun startPingTask() {
        pingJob?.cancel()
        pingJob = CoroutineScope(Dispatchers.IO).launch {
            // Временно отключаем автоматические ping, так как сервер сам управляет соединением
            // и может отключать неактивные соединения
            println("⚠️ Автоматические ping отключены (сервер сам управляет соединением)")
        }
    }

    private fun sendPing() {
        println("❤️ Отправка ping на сервер")
        val ping = PingMessage()
        sendMessage(ping)
    }

    // Новый Lifecycle API
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
            else -> {
                // Другие события не обрабатываем
            }
        }
    }

    fun isConnected(): Boolean {
        return connectionState.value is WebSocketState.Connected
    }
}