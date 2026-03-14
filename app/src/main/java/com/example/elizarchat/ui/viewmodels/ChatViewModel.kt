package com.example.elizarchat.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.elizarchat.data.local.session.TokenManager
import com.example.elizarchat.data.remote.ApiManager
import com.example.elizarchat.data.remote.dto.MessageDto
import com.example.elizarchat.data.remote.dto.SenderDto
import com.example.elizarchat.data.remote.websocket.WebSocketManager
import com.example.elizarchat.data.remote.websocket.WebSocketState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.time.Instant

data class ChatState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val messages: List<MessageDto> = emptyList(),
    val hasMoreMessages: Boolean = true,
    val currentPage: Int = 1,
    val chatInfo: ChatInfo? = null,
    val typingUsers: Set<Int> = emptySet(),
    val connectionStatus: WebSocketState = WebSocketState.Disconnected
)

data class ChatInfo(
    val id: Int,
    val name: String,
    val type: String,
    val members: List<ChatMember> = emptyList()
)

data class ChatMember(
    val userId: Int,
    val username: String? = null,
    val displayName: String? = null,
    val role: String = "member"
)

class ChatViewModel(
    private val apiManager: ApiManager,
    private val webSocketManager: WebSocketManager,
    private val tokenManager: TokenManager,
    private val chatId: Int
) : ViewModel() {

    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    private var typingJob: kotlinx.coroutines.Job? = null
    private val TYPING_TIMEOUT = 3000L // 3 секунды

    init {
        loadChatInfo()
        loadMessages()
        observeWebSocketEvents()
        subscribeToChat()
    }

    private fun subscribeToChat() {
        viewModelScope.launch {
            webSocketManager.subscribeToChat(chatId)
            println("✅ ChatViewModel: Подписались на чат $chatId через WebSocket")
        }
    }

    private fun observeWebSocketEvents() {
        // Новые сообщения
        viewModelScope.launch {
            webSocketManager.newMessages.collectLatest { event ->
                if (event.getEffectiveChatId() == chatId) {
                    println("📨 ChatViewModel: Получено новое сообщение через WebSocket")

                    val messageObj = event.message ?: event.data

                    if (messageObj != null) {
                        // Конвертируем metadata в JsonObject
                        val metadataJson = try {
                            if (messageObj.metadata is JsonObject) {
                                messageObj.metadata as JsonObject
                            } else {
                                JsonObject(emptyMap())
                            }
                        } catch (e: Exception) {
                            JsonObject(emptyMap())
                        }

                        // Создаем SenderDto если есть информация об отправителе
                        val senderDto = if (messageObj.senderId != null || messageObj.senderUsername != null) {
                            SenderDto(
                                id = messageObj.senderId ?: messageObj.senderIdAlt ?: 0,
                                username = messageObj.senderUsername,
                                displayName = messageObj.senderDisplayName,
                                avatarUrl = messageObj.senderAvatarUrl
                            )
                        } else null

                        val messageDto = MessageDto(
                            id = messageObj.id,
                            chatId = messageObj.getEffectiveChatId(),
                            userId = messageObj.getEffectiveSenderId(),
                            content = messageObj.content,
                            type = messageObj.type,
                            metadata = metadataJson,
                            replyTo = messageObj.replyTo,
                            status = messageObj.status ?: "sent",
                            createdAt = messageObj.getEffectiveCreatedAt(),
                            updatedAt = messageObj.updatedAt ?: messageObj.updatedAtAlt,
                            deletedAt = messageObj.deletedAt,
                            readBy = messageObj.readBy ?: emptyList(),
                            sender = senderDto
                        )

                        // Проверяем, нет ли дубликата
                        val isDuplicate = _state.value.messages.any { it.id == messageDto.id }
                        if (!isDuplicate) {
                            // Сортируем сообщения по времени (старые вверху, новые внизу)
                            val newMessages = (_state.value.messages + messageDto)
                                .sortedBy { it.createdAt }

                            _state.value = _state.value.copy(
                                messages = newMessages
                            )
                            println("✅ Добавлено сообщение ${messageDto.id} в состояние, всего: ${newMessages.size}")

                            // Автоматически отмечаем как прочитанные, если это не наше сообщение
                            viewModelScope.launch {
                                val currentUserId = getCurrentUserId()
                                if (messageDto.getEffectiveUserId() != currentUserId) {
                                    markMessagesAsRead(listOf(messageDto.id))
                                }
                            }
                        } else {
                            println("⚠️ Сообщение ${messageDto.id} уже существует, пропускаем")
                        }
                    }
                }
            }
        }

        // События печатания
        viewModelScope.launch {
            webSocketManager.typingEvents.collectLatest { event ->
                if (event.chatId == chatId) {
                    viewModelScope.launch {
                        val currentUserId = getCurrentUserId()
                        if (event.userId != currentUserId) {
                            if (event.isTyping) {
                                _state.value = _state.value.copy(
                                    typingUsers = _state.value.typingUsers + event.userId
                                )
                                println("⌨️ Пользователь ${event.userId} начал печатать")
                            } else {
                                _state.value = _state.value.copy(
                                    typingUsers = _state.value.typingUsers - event.userId
                                )
                                println("⌨️ Пользователь ${event.userId} закончил печатать")
                            }
                        }
                    }
                }
            }
        }

        // Статус подключения
        viewModelScope.launch {
            webSocketManager.connectionState.collectLatest { state ->
                _state.value = _state.value.copy(
                    connectionStatus = state
                )
                println("🔌 ChatViewModel: Статус подключения изменился на $state")
            }
        }

        // Подтверждения отправки сообщений
        viewModelScope.launch {
            webSocketManager.messageConfirmations.collectLatest { confirmation ->
                if (confirmation.chatId == chatId) {
                    println("✅ Получено подтверждение отправки сообщения: ${confirmation.messageId}")
                    // Обновляем статус сообщения в списке
                    _state.value = _state.value.copy(
                        messages = _state.value.messages.map { message ->
                            if (message.id < 0) { // Временное сообщение
                                message.copy(
                                    id = confirmation.messageId,
                                    status = "sent"
                                )
                            } else message
                        }
                    )
                }
            }
        }

        // Ошибки WebSocket
        viewModelScope.launch {
            webSocketManager.errors.collectLatest { error ->
                println("❌ Ошибка WebSocket: ${error.message}")
                _state.value = _state.value.copy(
                    error = "WebSocket error: ${error.message}"
                )
            }
        }
    }

    fun loadChatInfo() {
        viewModelScope.launch {
            try {
                val response = apiManager.getChatById(chatId)
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true) {
                        val chat = apiResponse.chat
                        if (chat != null) {
                            _state.value = _state.value.copy(
                                chatInfo = ChatInfo(
                                    id = chat.id,
                                    name = chat.name ?: "Chat",
                                    type = chat.type,
                                    members = chat.members?.map { member ->
                                        ChatMember(
                                            userId = member.userId,
                                            role = member.role
                                        )
                                    } ?: emptyList()
                                )
                            )
                            println("✅ Информация о чате загружена: ${chat.name}")
                        }
                    }
                }
            } catch (e: Exception) {
                println("❌ Ошибка загрузки информации о чате: ${e.message}")
            }
        }
    }

    fun loadMessages(refresh: Boolean = false) {
        viewModelScope.launch {
            try {
                if (refresh) {
                    _state.value = _state.value.copy(
                        isLoading = true,
                        messages = emptyList(),
                        currentPage = 1,
                        hasMoreMessages = true
                    )
                } else {
                    _state.value = _state.value.copy(isLoadingMore = true)
                }

                val page = if (refresh) 1 else _state.value.currentPage
                val limit = 50
                val offset = (page - 1) * limit

                val response = apiManager.getMessages(
                    chatId = chatId,
                    limit = limit,
                    offset = offset
                )

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true) {
                        val newMessages = apiResponse.messages ?: emptyList()

                        // Сортируем по времени (старые вверху, новые внизу)
                        val allMessages = if (refresh) {
                            newMessages.sortedBy { it.createdAt }
                        } else {
                            (_state.value.messages + newMessages)
                                .distinctBy { it.id }
                                .sortedBy { it.createdAt }
                        }

                        _state.value = _state.value.copy(
                            messages = allMessages,
                            hasMoreMessages = newMessages.size >= limit,
                            currentPage = if (refresh) 2 else page + 1,
                            isLoading = false,
                            isLoadingMore = false,
                            error = null
                        )

                        println("✅ Загружено ${newMessages.size} сообщений, всего: ${allMessages.size}")

                        // Отмечаем сообщения как прочитанные
                        if (newMessages.isNotEmpty()) {
                            viewModelScope.launch {
                                markMessagesAsRead(newMessages.map { it.id })
                            }
                        }
                    }
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        error = "HTTP ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    error = e.message
                )
                println("❌ Ошибка загрузки сообщений: ${e.message}")
            }
        }
    }

    fun loadMoreMessages() {
        if (!_state.value.isLoadingMore && _state.value.hasMoreMessages) {
            loadMessages()
        }
    }

    fun sendMessage(content: String, replyTo: Int? = null) {
        viewModelScope.launch {
            try {
                println("📤 Отправка сообщения через WebSocket: $content")

                val currentUserId = getCurrentUserId()

                // Оптимистичное добавление сообщения
                val tempMessage = MessageDto(
                    id = -System.currentTimeMillis().toInt(),
                    chatId = chatId,
                    userId = currentUserId,
                    content = content,
                    type = "text",
                    metadata = JsonObject(emptyMap()),
                    status = "sending",
                    createdAt = Instant.now().toString()
                )

                // Добавляем с сортировкой
                val newMessages = (_state.value.messages + tempMessage)
                    .sortedBy { it.createdAt }

                _state.value = _state.value.copy(
                    messages = newMessages
                )

                // Отправляем через WebSocket
                val success = webSocketManager.sendChatMessage(
                    chatId = chatId,
                    content = content,
                    replyTo = replyTo
                )

                if (!success) {
                    // Используем REST API как fallback
                    val response = apiManager.sendTextMessage(
                        chatId = chatId,
                        content = content,
                        replyTo = replyTo
                    )

                    if (response.isSuccessful) {
                        val apiResponse = response.body()
                        if (apiResponse?.success == true) {
                            println("✅ Сообщение отправлено через REST API")
                            apiResponse.data?.let { messageDto ->
                                // Заменяем временное сообщение на реальное
                                val updatedMessages = _state.value.messages
                                    .filter { it.id != tempMessage.id }
                                    .plus(messageDto)
                                    .sortedBy { it.createdAt }

                                _state.value = _state.value.copy(
                                    messages = updatedMessages
                                )
                            }
                        } else {
                            throw Exception(apiResponse?.error ?: "Failed to send message")
                        }
                    } else {
                        throw Exception("HTTP ${response.code()}")
                    }
                }

                // Отменяем индикатор печатания
                sendTypingStatus(false)

            } catch (e: Exception) {
                println("❌ Ошибка отправки сообщения: ${e.message}")
                _state.value = _state.value.copy(
                    error = "Не удалось отправить сообщение"
                )
            }
        }
    }

    fun sendTypingStatus(isTyping: Boolean) {
        // Отменяем предыдущую задачу
        typingJob?.cancel()

        if (isTyping) {
            // Запускаем новую задачу для отправки статуса печатания
            typingJob = viewModelScope.launch {
                webSocketManager.sendTypingStatus(chatId, true)

                // Отправляем статус "не печатает" через TYPING_TIMEOUT
                delay(TYPING_TIMEOUT)
                webSocketManager.sendTypingStatus(chatId, false)
            }
        } else {
            // Немедленно отправляем статус "не печатает"
            viewModelScope.launch {
                webSocketManager.sendTypingStatus(chatId, false)
            }
        }
    }

    fun markMessagesAsRead(messageIds: List<Int>) {
        viewModelScope.launch {
            try {
                webSocketManager.sendReadReceipt(chatId, messageIds)
                println("👁️ Отправлено подтверждение прочтения для ${messageIds.size} сообщений")
            } catch (e: Exception) {
                println("⚠️ Не удалось отметить сообщения как прочитанные: ${e.message}")
            }
        }
    }

    private suspend fun getCurrentUserId(): Int {
        // Получаем ID текущего пользователя из токена
        return tokenManager.getUserId()?.toIntOrNull() ?: 0
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    override fun onCleared() {
        super.onCleared()
        // Отписываемся от чата
        viewModelScope.launch {
            webSocketManager.unsubscribeFromChat(chatId)
            println("📤 ChatViewModel: Отписались от чата $chatId")
        }
        typingJob?.cancel()
    }

    companion object {
        fun provideFactory(
            apiManager: ApiManager,
            webSocketManager: WebSocketManager,
            tokenManager: TokenManager,
            chatId: Int
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ChatViewModel(apiManager, webSocketManager, tokenManager, chatId) as T
            }
        }
    }
}