package com.example.elizarchat.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.elizarchat.data.local.RefreshEvent
import com.example.elizarchat.data.local.RefreshManager
import com.example.elizarchat.data.local.session.TokenManager
import com.example.elizarchat.data.mapper.ChatMapper
import com.example.elizarchat.data.mapper.UserMapper
import com.example.elizarchat.data.remote.ApiManager
import com.example.elizarchat.data.remote.dto.ChatDto
import com.example.elizarchat.data.remote.dto.UserDto
import com.example.elizarchat.domain.model.Chat
import com.example.elizarchat.domain.model.ChatMember
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.Instant
import java.time.format.DateTimeParseException

data class ChatsState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val chats: List<Chat> = emptyList(),
    val isRefreshing: Boolean = false,
    val hasMoreChats: Boolean = true,
    val currentPage: Int = 1
)

class ChatsViewModel(
    private val apiManager: ApiManager,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(ChatsState())
    val state: StateFlow<ChatsState> = _state.asStateFlow()

    private val _usersCache = MutableStateFlow<Map<Int, UserDto>>(emptyMap())
    val usersCache: StateFlow<Map<Int, UserDto>> = _usersCache.asStateFlow()

    val currentUserId: Int = runBlocking {
        tokenManager.getUserId()?.toIntOrNull() ?: 0
    }

    init {
        loadChats()

        viewModelScope.launch {
            RefreshManager.events.collect { event ->
                when (event) {
                    is RefreshEvent.RefreshChats -> {
                        println("🔄 ChatsViewModel: Обновление списка чатов")
                        loadChats(refresh = true)
                    }
                    is RefreshEvent.RefreshSpecificChat -> {
                        println("🔄 ChatsViewModel: Обновление конкретного чата ${event.chatId}")
                        refreshSpecificChat(event.chatId)
                    }
                    is RefreshEvent.RefreshMessages -> {
                        println("ℹ️ ChatsViewModel: Получено событие RefreshMessages (игнорируем)")
                    }
                }
            }
        }
    }

    fun loadChats(refresh: Boolean = false) {
        viewModelScope.launch {
            try {
                if (refresh) {
                    _state.update {
                        it.copy(
                            isRefreshing = true,
                            currentPage = 1,
                            hasMoreChats = true,
                            chats = emptyList()
                        )
                    }
                } else {
                    _state.update { it.copy(isLoading = true) }
                }

                println("📡 Загрузка чатов, страница: ${_state.value.currentPage}")

                val response = apiManager.getChats(
                    page = _state.value.currentPage,
                    limit = 20
                )

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    val chatDtos = apiResponse?.chats ?: emptyList()

                    println("✅ Получено чатов: ${chatDtos.size}")

                    // Сначала загружаем всех пользователей для приватных чатов
                    val userIdsToLoad = extractUserIdsFromChats(chatDtos)
                    if (userIdsToLoad.isNotEmpty()) {
                        println("📡 Загрузка ${userIdsToLoad.size} пользователей...")
                        loadUsersBatch(userIdsToLoad)
                        // Даем время на загрузку
                        kotlinx.coroutines.delay(300)
                    }

                    // Конвертируем DTO в доменные модели с информацией о пользователях
                    val domainChats = chatDtos.map { chatDto ->
                        buildChatWithUserInfo(chatDto)
                    }

                    val currentChats = if (refresh) emptyList() else _state.value.chats
                    val newChats = currentChats + domainChats

                    val hasMore = chatDtos.size >= 20
                    val nextPage = if (refresh) 2 else _state.value.currentPage + 1

                    _state.update {
                        it.copy(
                            chats = newChats,
                            hasMoreChats = hasMore,
                            currentPage = nextPage,
                            isLoading = false,
                            isRefreshing = false,
                            error = null
                        )
                    }

                    println("📊 Всего чатов в состоянии: ${_state.value.chats.size}")
                    _state.value.chats.forEach { chat ->
                        println("   Чат: id=${chat.id}, type=${chat.type}, displayName=${chat.displayName(currentUserId.toString())}")
                    }
                } else {
                    println("❌ HTTP ошибка: ${response.code()}")
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            error = "HTTP ${response.code()}"
                        )
                    }
                }
            } catch (e: Exception) {
                println("❌ Исключение: ${e.message}")
                e.printStackTrace()
                _state.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = "Network error: ${e.message}"
                    )
                }
            }
        }
    }

    private fun extractUserIdsFromChats(chats: List<ChatDto>): Set<Int> {
        val userIds = mutableSetOf<Int>()

        chats.forEach { chat ->
            if (chat.type == "private") {
                // Пытаемся извлечь ID из названия чата (private_1_2)
                val extractedId = extractOtherUserIdFromName(chat.name)
                if (extractedId != null && extractedId != currentUserId) {
                    userIds.add(extractedId)
                }

                // Также проверяем members, если есть
                chat.members?.forEach { member ->
                    if (member.userId != currentUserId) {
                        userIds.add(member.userId)
                    }
                }
            }
        }

        return userIds
    }

    private suspend fun loadUsersBatch(userIds: Set<Int>) {
        val deferredUsers = userIds.map { userId ->
            viewModelScope.async {
                loadUserInfoIfNeeded(userId)
            }
        }
        deferredUsers.awaitAll()
    }

    private suspend fun loadUserInfoIfNeeded(userId: Int): UserDto? {
        // Проверяем кэш
        _usersCache.value[userId]?.let { return it }

        // Загружаем из API
        return try {
            println("📡 Загрузка пользователя $userId")
            val response = apiManager.getUserById(userId)
            if (response.isSuccessful) {
                val apiResponse = response.body()
                val user = apiResponse?.user
                if (user != null) {
                    _usersCache.update { it + (userId to user) }
                    println("✅ Загружен пользователь: ${user.displayName ?: user.username} (ID=$userId)")
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

    private fun buildChatWithUserInfo(chatDto: ChatDto): Chat {
        val participants = mutableListOf<ChatMember>()

        if (chatDto.type == "private") {
            // Для приватного чата находим собеседника
            val otherUserId = findOtherUserId(chatDto)

            if (otherUserId != null) {
                val userDto = _usersCache.value[otherUserId]
                if (userDto != null) {
                    participants.add(
                        ChatMember(
                            id = "0",
                            chatId = chatDto.id.toString(),
                            userId = otherUserId.toString(),
                            role = "member",
                            joinedAt = Instant.now(),
                            unreadCount = 0,
                            lastReadMessageId = null,
                            notificationsEnabled = true,
                            isHidden = false,
                            user = UserMapper.dtoToDomain(userDto)
                        )
                    )
                }
            }
        } else {
            // Для групповых чатов используем members если есть
            chatDto.members?.forEach { memberDto ->
                val userDto = _usersCache.value[memberDto.userId]
                if (userDto != null) {
                    participants.add(
                        ChatMember(
                            id = memberDto.id?.toString() ?: "0",
                            chatId = chatDto.id.toString(),
                            userId = memberDto.userId.toString(),
                            role = memberDto.role,
                            joinedAt = Instant.now(),
                            unreadCount = memberDto.unreadCount,
                            lastReadMessageId = memberDto.lastReadMessageId?.toString(),
                            notificationsEnabled = true,
                            isHidden = false,
                            user = UserMapper.dtoToDomain(userDto)
                        )
                    )
                }
            }
        }

        return ChatMapper.dtoToDomain(chatDto).copy(
            participants = participants
        )
    }

    private fun findOtherUserId(chatDto: ChatDto): Int? {
        // 1. Пытаемся найти в members
        val otherMember = chatDto.members?.find { it.userId != currentUserId }
        if (otherMember != null) {
            return otherMember.userId
        }

        // 2. Извлекаем из имени чата (private_1_2)
        return extractOtherUserIdFromName(chatDto.name)
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

    private suspend fun refreshSpecificChat(chatId: Int) {
        viewModelScope.launch {
            try {
                println("🔄 Обновление конкретного чата: $chatId")

                val response = apiManager.getChatById(chatId)

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    val updatedChatDto = apiResponse?.chat

                    if (updatedChatDto != null) {
                        // Загружаем пользователя если нужно
                        if (updatedChatDto.type == "private") {
                            val otherUserId = findOtherUserId(updatedChatDto)
                            if (otherUserId != null) {
                                loadUserInfoIfNeeded(otherUserId)
                            }
                        }

                        val updatedChat = buildChatWithUserInfo(updatedChatDto)
                        val currentChats = _state.value.chats.toMutableList()
                        val index = currentChats.indexOfFirst { it.id == chatId.toString() }

                        if (index != -1) {
                            currentChats[index] = updatedChat
                            _state.update { it.copy(chats = currentChats) }
                            println("✅ Чат $chatId обновлен")
                        }
                    }
                }
            } catch (e: Exception) {
                println("❌ Ошибка обновления чата $chatId: ${e.message}")
            }
        }
    }

    fun refreshChats() {
        loadChats(refresh = true)
    }

    fun loadMoreChats() {
        if (!_state.value.isLoading && _state.value.hasMoreChats) {
            loadChats()
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun logout() {
        viewModelScope.launch {
            tokenManager.clearTokens()
        }
    }

    companion object {
        fun provideFactory(
            apiManager: ApiManager,
            tokenManager: TokenManager
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ChatsViewModel(apiManager, tokenManager) as T
            }
        }
    }
}