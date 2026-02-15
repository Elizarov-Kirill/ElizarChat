package com.example.elizarchat.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.elizarchat.data.local.session.TokenManager
import com.example.elizarchat.data.remote.ApiManager
import com.example.elizarchat.data.remote.dto.ChatDto
import com.example.elizarchat.data.remote.dto.MessageDto
import com.example.elizarchat.data.remote.dto.SendMessageRequest
import com.example.elizarchat.data.remote.websocket.WebSocketManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class ChatState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val chat: ChatDto? = null,
    val messages: List<MessageDto> = emptyList(),
    val isRefreshing: Boolean = false,
    val hasMoreMessages: Boolean = true,
    val currentOffset: Int = 0,
    val isSending: Boolean = false,
    val newMessageText: String = "",
    val isLoaded: Boolean = false,
    val currentUserId: Int? = null,
    val isConnectedToWebSocket: Boolean = false,
    val typingUsers: Set<Int> = emptySet()
)

class ChatViewModel(
    private val apiManager: ApiManager,
    private val tokenManager: TokenManager,
    private val webSocketManager: WebSocketManager
) : ViewModel() {

    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    private var currentChatId: Int? = null

    init {
        // Загружаем ID текущего пользователя
        viewModelScope.launch {
            val userId = tokenManager.getUserId()?.toIntOrNull()
            println("👤 ChatViewModel: Текущий пользователь ID = $userId")
            _state.value = _state.value.copy(currentUserId = userId)
        }

        // Следим за состоянием WebSocket
        viewModelScope.launch {
            webSocketManager.connectionState.collect { wsState ->
                val isConnected = when (wsState) {
                    is com.example.elizarchat.data.remote.websocket.WebSocketState.Connected -> true
                    else -> false
                }
                _state.value = _state.value.copy(isConnectedToWebSocket = isConnected)
            }
        }

        // Подписываемся на новые сообщения из WebSocket
        viewModelScope.launch {
            webSocketManager.newMessages.collectLatest { event ->
                if (event.chatId == currentChatId) {
                    println("📨 ChatViewModel: Получено новое сообщение через WebSocket")
                    // Проверяем, нет ли дубликата
                    val isDuplicate = _state.value.messages.any { it.id == event.message.id }
                    if (!isDuplicate) {
                        _state.value = _state.value.copy(
                            messages = (_state.value.messages + event.message) as List<MessageDto>
                        )
                    }
                }
            }
        }

        // Подписываемся на индикаторы печатания
        viewModelScope.launch {
            webSocketManager.typingEvents.collectLatest { event ->
                if (event.chatId == currentChatId) {
                    if (event.isTyping) {
                        // Добавляем пользователя в набор печатающих
                        _state.value = _state.value.copy(
                            typingUsers = _state.value.typingUsers + event.userId
                        )
                    } else {
                        // Удаляем пользователя
                        _state.value = _state.value.copy(
                            typingUsers = _state.value.typingUsers - event.userId
                        )
                    }
                }
            }
        }

        // Подписываемся на подтверждения отправки
        viewModelScope.launch {
            webSocketManager.messageConfirmations.collectLatest { confirmation ->
                if (confirmation.chatId == currentChatId) {
                    println("✅ Получено подтверждение отправки сообщения: ${confirmation.messageId}")
                    // Можно обновить статус сообщения с временного ID на реальный
                    // Это требует дополнительной логики для отслеживания временных сообщений
                }
            }
        }
    }

    fun setChatId(chatId: Int) {
        if (currentChatId == chatId) return

        currentChatId = chatId
        _state.value = ChatState() // Сброс состояния

        // Подписываемся на события чата через WebSocket
        viewModelScope.launch {
            if (webSocketManager.isConnected()) {
                webSocketManager.subscribeToChat(chatId)
                println("✅ ChatViewModel: Подписались на чат $chatId через WebSocket")
            }
        }

        // Загружаем данные
        loadChatInfo()
        loadMessages(refresh = true)
    }

    fun loadChatInfo() {
        val chatId = currentChatId ?: return

        viewModelScope.launch {
            try {
                val response = apiManager.getChatById(chatId)
                if (response.isSuccessful && response.body()?.success == true) {
                    val chat = response.body()?.data
                    _state.value = _state.value.copy(
                        chat = chat,
                        error = null
                    )
                } else {
                    _state.value = _state.value.copy(
                        error = response.body()?.error ?: "Failed to load chat"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Network error: ${e.message}"
                )
            }
        }
    }

    fun loadMessages(refresh: Boolean = false) {
        val chatId = currentChatId ?: return

        viewModelScope.launch {
            try {
                if (refresh) {
                    _state.value = _state.value.copy(
                        isRefreshing = true,
                        currentOffset = 0,
                        hasMoreMessages = true,
                        messages = emptyList()
                    )
                } else {
                    _state.value = _state.value.copy(isLoading = true)
                }

                val response = apiManager.getMessages(
                    chatId = chatId,
                    limit = 50,
                    offset = _state.value.currentOffset
                )

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true) {
                        val messagesResponse = apiResponse.data
                        if (messagesResponse != null) {
                            val currentMessages = _state.value.messages
                            val newMessages = if (refresh) {
                                messagesResponse.messages
                            } else {
                                currentMessages + messagesResponse.messages
                            }

                            _state.value = _state.value.copy(
                                messages = newMessages,
                                hasMoreMessages = messagesResponse.hasMore,
                                currentOffset = if (refresh) messagesResponse.messages.size
                                else _state.value.currentOffset + messagesResponse.messages.size,
                                isLoading = false,
                                isRefreshing = false,
                                isLoaded = true,
                                error = null
                            )
                        }
                    } else {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            isRefreshing = false,
                            error = apiResponse?.error ?: "Failed to load messages"
                        )
                    }
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = "HTTP ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    error = "Network error: ${e.message}"
                )
            }
        }
    }

    fun updateMessageText(text: String) {
        _state.value = _state.value.copy(newMessageText = text)

        // Отправляем статус печатания через WebSocket
        currentChatId?.let { chatId ->
            val isTyping = text.isNotEmpty()
            webSocketManager.sendTypingStatus(chatId, isTyping)
        }
    }

    fun sendMessage() {
        val chatId = currentChatId ?: return
        val text = _state.value.newMessageText.trim()
        if (text.isEmpty()) return

        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isSending = true)

                // Оптимистичное обновление UI
                val tempMessageId = System.currentTimeMillis().toInt()
                val tempMessage = MessageDto(
                    id = tempMessageId,
                    content = text,
                    senderId = _state.value.currentUserId ?: 0,
                    chatId = chatId,
                    type = "text",
                    status = "sending",
                    createdAt = java.time.Instant.now().toString()
                )

                _state.value = _state.value.copy(
                    messages = _state.value.messages + tempMessage,
                    newMessageText = "",
                    error = null
                )

                // Пробуем отправить через WebSocket в первую очередь
                var wsSuccess = false
                if (webSocketManager.isConnected()) {
                    wsSuccess = webSocketManager.sendChatMessage(chatId, text)
                    println("📤 ChatViewModel: Отправка через WebSocket, успех: $wsSuccess")
                }

                // Если WebSocket не доступен, используем REST API как fallback
                if (!wsSuccess) {
                    println("📤 ChatViewModel: WebSocket не доступен, используем REST API")

                    // ИСПРАВЛЕНО: отправляем null или пустую JSON строку
                    val request = SendMessageRequest(
                        content = text,
                        type = "text",
                        metadata = {}, // ИЛИ "{}" для пустого JSON
                        replyTo = null
                    )

                    val response = apiManager.sendMessage(chatId, request)
                    if (response.isSuccessful && response.body()?.success == true) {
                        val sentMessage = response.body()?.data
                        if (sentMessage != null) {
                            // Заменяем временное сообщение на настоящее
                            _state.value = _state.value.copy(
                                messages = _state.value.messages.filter { it.id != tempMessageId } + sentMessage
                            )
                        }
                    } else {
                        // Помечаем сообщение как ошибочное
                        _state.value = _state.value.copy(
                            messages = _state.value.messages.map { msg ->
                                if (msg.id == tempMessageId) {
                                    msg.copy(status = "error")
                                } else {
                                    msg
                                }
                            },
                            error = response.body()?.error ?: "Failed to send message"
                        )
                    }
                }

                _state.value = _state.value.copy(isSending = false)

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isSending = false,
                    error = "Network error: ${e.message}"
                )

                // Помечаем сообщение как ошибочное
                _state.value = _state.value.copy(
                    messages = _state.value.messages.map { msg ->
                        if (msg.content == text && msg.status == "sending") {
                            msg.copy(status = "error")
                        } else {
                            msg
                        }
                    }
                )
            }
        }
    }

    fun markAsRead() {
        currentChatId?.let { chatId ->
            // Получаем все непрочитанные сообщения в этом чате
            val currentUserId = _state.value.currentUserId
            val unreadMessageIds = _state.value.messages
                .filter {
                    currentUserId != null &&
                            it.senderId != currentUserId && // Не наши сообщения
                            it.status != "read"
                }
                .map { it.id }

            if (unreadMessageIds.isNotEmpty()) {
                // TODO: Реализовать отправку read receipt через API
                println("👁️ Marking messages as read: $unreadMessageIds")
            }
        }
    }

    fun refreshMessages() {
        loadMessages(refresh = true)
    }

    fun loadMoreMessages() {
        if (!_state.value.isLoading && _state.value.hasMoreMessages) {
            loadMessages()
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    // Добавить в ChatViewModel.kt
    fun retrySendMessage(failedMessage: MessageDto) {
        val chatId = currentChatId ?: return
        val text = failedMessage.content

        viewModelScope.launch {
            try {
                // Удаляем старое сообщение с ошибкой
                _state.value = _state.value.copy(
                    messages = _state.value.messages.filter { it.id != failedMessage.id }
                )

                // Пробуем отправить заново через WebSocket
                var wsSuccess = false
                if (webSocketManager.isConnected()) {
                    wsSuccess = webSocketManager.sendChatMessage(chatId, text)
                }

                // Если WebSocket не доступен, используем REST API
                if (!wsSuccess) {
                    // ИСПРАВЛЕНО
                    val request = SendMessageRequest(
                        content = text,
                        type = "text",
                        metadata = {}, // ИЛИ "{}"
                        replyTo = null
                    )

                    val response = apiManager.sendMessage(chatId, request)
                    if (response.isSuccessful && response.body()?.success == true) {
                        val sentMessage = response.body()?.data
                        if (sentMessage != null) {
                            _state.value = _state.value.copy(
                                messages = _state.value.messages + sentMessage,
                                error = null
                            )
                        }
                    } else {
                        _state.value = _state.value.copy(
                            error = "Failed to resend message"
                        )
                    }
                }

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Retry failed: ${e.message}"
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Отмечаем сообщения как прочитанные при закрытии чата
        markAsRead()

        // Отписываемся от чата в WebSocket
        currentChatId?.let { chatId ->
            if (webSocketManager.isConnected()) {
                webSocketManager.unsubscribeFromChat(chatId)
            }
        }
    }

    companion object {
        fun provideFactory(
            apiManager: ApiManager,
            tokenManager: TokenManager,
            webSocketManager: WebSocketManager // ДОБАВЛЕНО
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ChatViewModel(apiManager, tokenManager, webSocketManager) as T
            }
        }
    }
}