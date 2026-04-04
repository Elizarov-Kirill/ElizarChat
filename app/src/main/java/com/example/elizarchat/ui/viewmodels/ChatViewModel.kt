package com.example.elizarchat.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.elizarchat.data.local.RefreshManager
import com.example.elizarchat.data.local.session.TokenManager
import com.example.elizarchat.data.mapper.UserMapper
import com.example.elizarchat.data.remote.ApiManager
import com.example.elizarchat.data.remote.dto.ChatDto
import com.example.elizarchat.data.remote.dto.MessageDto
import com.example.elizarchat.data.remote.dto.SenderDto
import com.example.elizarchat.data.remote.dto.UserDto
import com.example.elizarchat.data.remote.websocket.WebSocketManager
import com.example.elizarchat.data.remote.websocket.WebSocketState
import com.example.elizarchat.domain.model.ChatMember
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonObject
import java.time.Instant

data class ChatState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isLoadingInfo: Boolean = false,
    val isSending: Boolean = false,  // ДОБАВЛЕНО
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

class ChatViewModel(
    private val apiManager: ApiManager,
    private val webSocketManager: WebSocketManager,
    private val tokenManager: TokenManager,
    private val chatId: Int
) : ViewModel() {

    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    // Кэш пользователей для отображения имен
    private val _usersCache = MutableStateFlow<Map<Int, UserDto>>(emptyMap())
    val usersCache: StateFlow<Map<Int, UserDto>> = _usersCache.asStateFlow()

    private var typingJob: kotlinx.coroutines.Job? = null
    private val TYPING_TIMEOUT = 3000L

    // ID текущего пользователя
    private val currentUserId: Int = runBlocking {
        tokenManager.getUserId()?.toIntOrNull() ?: 0
    }

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
                        val metadataJson = try {
                            if (messageObj.metadata is JsonObject) {
                                messageObj.metadata as JsonObject
                            } else {
                                JsonObject(emptyMap())
                            }
                        } catch (e: Exception) {
                            JsonObject(emptyMap())
                        }

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

                        val isDuplicate = _state.value.messages.any { it.id == messageDto.id }
                        if (!isDuplicate) {
                            val newMessages = (_state.value.messages + messageDto)
                                .sortedBy { it.createdAt }

                            _state.update { it.copy(messages = newMessages) }
                            println("✅ Добавлено сообщение ${messageDto.id} в состояние, всего: ${newMessages.size}")

                            viewModelScope.launch {
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
                    if (event.userId != currentUserId) {
                        if (event.isTyping) {
                            _state.update {
                                it.copy(typingUsers = it.typingUsers + event.userId)
                            }
                            println("⌨️ Пользователь ${event.userId} начал печатать")
                        } else {
                            _state.update {
                                it.copy(typingUsers = it.typingUsers - event.userId)
                            }
                            println("⌨️ Пользователь ${event.userId} закончил печатать")
                        }
                    }
                }
            }
        }

        // Статус подключения
        viewModelScope.launch {
            webSocketManager.connectionState.collectLatest { state ->
                _state.update { it.copy(connectionStatus = state) }
                println("🔌 ChatViewModel: Статус подключения изменился на $state")
            }
        }

        // Подтверждения отправки сообщений
        viewModelScope.launch {
            webSocketManager.messageConfirmations.collectLatest { confirmation ->
                if (confirmation.chatId == chatId) {
                    println("✅ Получено подтверждение отправки сообщения: ${confirmation.messageId}")

                    _state.update { state ->
                        state.copy(
                            messages = state.messages.map { message ->
                                if (message.id < 0) {
                                    message.copy(
                                        id = confirmation.messageId,
                                        status = "sent"
                                    )
                                } else message
                            }
                        )
                    }

                    try {
                        RefreshManager.notifyChatsChanged()
                        println("🔄 Список чатов обновлен (подтверждение отправки для чата $chatId)")
                    } catch (e: Exception) {
                        println("❌ Ошибка уведомления: ${e.message}")
                    }
                }
            }
        }

        // Ошибки WebSocket
        viewModelScope.launch {
            webSocketManager.errors.collectLatest { error ->
                println("❌ Ошибка WebSocket: ${error.message}")
                _state.update { it.copy(error = "WebSocket error: ${error.message}") }
            }
        }
    }

    private fun loadChatInfo() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoadingInfo = true) }

                val response = apiManager.getChatById(chatId)

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    val chat = apiResponse?.chat
                    if (chat != null) {
                        val members = buildChatMembers(chat)
                        val displayName = if (chat.type == "private") {
                            getPrivateChatDisplayName(chat)
                        } else {
                            chat.name ?: "Group Chat"
                        }

                        val chatInfo = ChatInfo(
                            id = chat.id,
                            name = displayName,
                            type = chat.type,
                            members = members
                        )

                        _state.update {
                            it.copy(
                                chatInfo = chatInfo,
                                isLoadingInfo = false,
                                error = null
                            )
                        }
                        println("✅ Загружена информация о чате: ${chatInfo.name}")
                    } else {
                        _state.update {
                            it.copy(
                                error = "Чат не найден",
                                isLoadingInfo = false
                            )
                        }
                    }
                } else {
                    val errorMsg = when (response.code()) {
                        401 -> "Сессия истекла. Пожалуйста, войдите снова."
                        403 -> "Нет доступа к этому чату"
                        404 -> "Чат не найден"
                        else -> "Ошибка загрузки чата: ${response.code()}"
                    }
                    _state.update {
                        it.copy(
                            error = errorMsg,
                            isLoadingInfo = false
                        )
                    }
                }
            } catch (e: Exception) {
                println("❌ Ошибка загрузки информации о чате: ${e.message}")
                _state.update {
                    it.copy(
                        error = "Ошибка сети: ${e.message}",
                        isLoadingInfo = false
                    )
                }
            }
        }
    }

    private suspend fun buildChatMembers(chat: ChatDto): List<ChatMember> {
        val members = mutableListOf<ChatMember>()

        chat.members?.forEach { memberDto ->
            val user = loadUserInfoIfNeeded(memberDto.userId)

            members.add(
                ChatMember(
                    id = memberDto.id?.toString() ?: "0",
                    chatId = chat.id.toString(),
                    userId = memberDto.userId.toString(),
                    role = memberDto.role,
                    joinedAt = java.time.Instant.now(),
                    unreadCount = memberDto.unreadCount,
                    lastReadMessageId = memberDto.lastReadMessageId?.toString(),
                    notificationsEnabled = true,
                    isHidden = false,
                    user = user?.let { UserMapper.dtoToDomain(it) }
                )
            )
        }

        return members
    }

    private suspend fun getPrivateChatDisplayName(chat: ChatDto): String {
        if (chat.type != "private") {
            return chat.name ?: "Chat"
        }

        // 1. Пытаемся найти в members
        val otherMember = chat.members?.find { it.userId != currentUserId }
        if (otherMember != null) {
            val user = loadUserInfoIfNeeded(otherMember.userId)
            return user?.displayName ?: user?.username ?: "User ${otherMember.userId}"
        }

        // 2. Извлекаем ID собеседника из имени чата (private_1_2)
        val otherUserId = extractOtherUserIdFromName(chat.name)
        if (otherUserId != null && otherUserId != currentUserId) {
            val user = loadUserInfoIfNeeded(otherUserId)
            return user?.displayName ?: user?.username ?: "User $otherUserId"
        }

        return chat.name ?: "Chat"
    }

    private suspend fun loadUserInfoIfNeeded(userId: Int): UserDto? {
        // Проверяем кэш
        _usersCache.value[userId]?.let { return it }

        // Загружаем из API
        return try {
            println("📡 Загрузка пользователя для чата: $userId")
            val response = apiManager.getUserById(userId)
            if (response.isSuccessful) {
                val apiResponse = response.body()
                val user = apiResponse?.user
                if (user != null) {
                    _usersCache.update { it + (userId to user) }
                    println("✅ Загружен пользователь для чата: ${user.displayName ?: user.username} (ID=$userId)")
                }
                user
            } else {
                println("❌ Ошибка загрузки пользователя $userId: HTTP ${response.code()}")
                null
            }
        } catch (e: Exception) {
            println("❌ Ошибка загрузки пользователя $userId: ${e.message}")
            null
        }
    }

    private fun extractOtherUserIdFromName(chatName: String?): Int? {
        if (chatName == null || !chatName.startsWith("private_")) return null
        val ids = chatName.split("_")
        if (ids.size < 3) return null
        val id1 = ids[1].toIntOrNull()
        val id2 = ids[2].toIntOrNull()
        return when (currentUserId) {
            id1 -> id2
            id2 -> id1
            else -> null
        }
    }

    fun loadMessages(refresh: Boolean = false) {
        viewModelScope.launch {
            try {
                if (refresh) {
                    _state.update {
                        it.copy(
                            isLoading = true,
                            messages = emptyList(),
                            currentPage = 1,
                            hasMoreMessages = true
                        )
                    }
                } else {
                    _state.update { it.copy(isLoadingMore = true) }
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

                        val allMessages = if (refresh) {
                            newMessages.sortedBy { it.createdAt }
                        } else {
                            (_state.value.messages + newMessages)
                                .distinctBy { it.id }
                                .sortedBy { it.createdAt }
                        }

                        _state.update {
                            it.copy(
                                messages = allMessages,
                                hasMoreMessages = newMessages.size >= limit,
                                currentPage = if (refresh) 2 else page + 1,
                                isLoading = false,
                                isLoadingMore = false,
                                error = null
                            )
                        }

                        println("✅ Загружено ${newMessages.size} сообщений, всего: ${allMessages.size}")

                        if (newMessages.isNotEmpty()) {
                            viewModelScope.launch {
                                markMessagesAsRead(newMessages.map { it.id })
                            }
                        }
                    } else {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                isLoadingMore = false,
                                error = apiResponse?.error ?: "Unknown error"
                            )
                        }
                    }
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            error = "HTTP ${response.code()}"
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        error = e.message
                    )
                }
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
                _state.update { it.copy(isSending = true) }  // Установка isSending = true

                println("📤 Отправка сообщения через WebSocket: $content")

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

                val newMessages = (_state.value.messages + tempMessage)
                    .sortedBy { it.createdAt }

                _state.update { it.copy(messages = newMessages) }

                val success = webSocketManager.sendChatMessage(
                    chatId = chatId,
                    content = content,
                    replyTo = replyTo
                )

                if (!success) {
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
                                val updatedMessages = _state.value.messages
                                    .filter { it.id != tempMessage.id }
                                    .plus(messageDto)
                                    .sortedBy { it.createdAt }

                                _state.update { it.copy(messages = updatedMessages) }

                                try {
                                    RefreshManager.notifyChatsChanged()
                                    println("🔄 Список чатов обновлен (отправлено сообщение в чат $chatId)")
                                } catch (e: Exception) {
                                    println("❌ Ошибка уведомления: ${e.message}")
                                }
                            }
                        } else {
                            throw Exception(apiResponse?.error ?: "Failed to send message")
                        }
                    } else {
                        throw Exception("HTTP ${response.code()}")
                    }
                } else {
                    try {
                        RefreshManager.notifyChatsChanged()
                        println("🔄 Список чатов обновлен (отправлено сообщение через WebSocket в чат $chatId)")
                    } catch (e: Exception) {
                        println("❌ Ошибка уведомления: ${e.message}")
                    }
                }

                sendTypingStatus(false)
                _state.update { it.copy(isSending = false) }  // Сброс isSending = false

            } catch (e: Exception) {
                println("❌ Ошибка отправки сообщения: ${e.message}")
                _state.update {
                    it.copy(
                        error = "Не удалось отправить сообщение",
                        isSending = false  // Сброс isSending = false при ошибке
                    )
                }
            }
        }
    }

    fun sendTypingStatus(isTyping: Boolean) {
        typingJob?.cancel()

        if (isTyping) {
            typingJob = viewModelScope.launch {
                webSocketManager.sendTypingStatus(chatId, true)
                delay(TYPING_TIMEOUT)
                webSocketManager.sendTypingStatus(chatId, false)
            }
        } else {
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

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    override fun onCleared() {
        super.onCleared()
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