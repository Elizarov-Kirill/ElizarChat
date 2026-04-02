package com.example.elizarchat.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.elizarchat.data.local.RefreshManager
import com.example.elizarchat.data.local.session.TokenManager
import com.example.elizarchat.data.remote.ApiManager
import com.example.elizarchat.data.remote.dto.CreateChatRequest
import com.example.elizarchat.data.remote.dto.ChatDto
import com.example.elizarchat.data.remote.dto.UserDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CreateChatState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val searchResults: List<UserDto> = emptyList(),
    val selectedUsers: List<UserDto> = emptyList(),
    val isSearching: Boolean = false,
    val isCreating: Boolean = false,
    val createdChat: ChatDto? = null,
    val showNameDialog: Boolean = false,
    val chatName: String = "",
    val chatType: String = "private"
)

class CreateChatViewModel(
    private val apiManager: ApiManager,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(CreateChatState())
    val state: StateFlow<CreateChatState> = _state.asStateFlow()

    fun updateSearchQuery(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        if (query.length >= 2) {
            searchUsers()
        } else {
            _state.value = _state.value.copy(searchResults = emptyList())
        }
    }

    fun searchUsers() {
        val query = _state.value.searchQuery
        if (query.length < 2) return

        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isSearching = true)

                val response = apiManager.searchUsers(query = query)

                if (response.isSuccessful) {
                    val usersResponse = response.body()
                    val users = usersResponse?.users ?: emptyList()
                    _state.value = _state.value.copy(
                        searchResults = users.filter { user ->
                            !_state.value.selectedUsers.any { it.id == user.id }
                        },
                        isSearching = false,
                        error = null
                    )
                } else {
                    val errorMsg = "HTTP ${response.code()}: ${response.message()}"
                    _state.value = _state.value.copy(
                        error = errorMsg,
                        isSearching = false
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Network error: ${e.message}",
                    isSearching = false
                )
            }
        }
    }

    fun addUser(user: UserDto) {
        val current = _state.value.selectedUsers
        if (current.none { it.id == user.id }) {
            _state.value = _state.value.copy(
                selectedUsers = current + user,
                searchResults = _state.value.searchResults.filter { it.id != user.id }
            )
        }
    }

    fun removeUser(userId: Int) {
        val current = _state.value.selectedUsers
        _state.value = _state.value.copy(
            selectedUsers = current.filter { it.id != userId }
        )
    }

    fun showNameDialog() {
        _state.value = _state.value.copy(showNameDialog = true)
    }

    fun hideNameDialog() {
        _state.value = _state.value.copy(showNameDialog = false)
    }

    fun updateChatName(name: String) {
        _state.value = _state.value.copy(chatName = name)
    }

    fun updateChatType(type: String) {
        _state.value = _state.value.copy(chatType = type)
    }

    fun createChat() {
        val selectedIds = _state.value.selectedUsers.map { it.id }
        if (selectedIds.isEmpty()) {
            _state.value = _state.value.copy(error = "Select at least one user")
            return
        }

        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isCreating = true)

                val userIds = selectedIds

                // 🔥 НОВАЯ ЛОГИКА ФОРМИРОВАНИЯ ИМЕНИ
                val chatName = when {
                    // 1. Если пользователь ввел имя (через диалог) — используем его
                    _state.value.chatName.isNotBlank() -> {
                        _state.value.chatName
                    }
                    // 2. Приватный чат (1 пользователь) — формат private_ID1_ID2
                    _state.value.selectedUsers.size == 1 -> {
                        val currentUserId = tokenManager.getUserId()?.toIntOrNull() ?: 0
                        val otherUser = _state.value.selectedUsers.first()
                        val otherUserId = otherUser.id

                        // Сортируем ID, чтобы имя было одинаковым для обоих участников
                        val sortedIds = listOf(currentUserId, otherUserId).sorted()
                        "private_${sortedIds[0]}_${sortedIds[1]}"
                    }
                    // 3. Групповой чат без имени
                    else -> {
                        "Group Chat"
                    }
                }

                // Определяем тип чата
                val chatType = if (_state.value.selectedUsers.size == 1) "private" else "group"

                val request = CreateChatRequest(
                    type = chatType,
                    name = chatName,
                    userIds = userIds
                )

                val response = apiManager.createChat(request)

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    val chat = apiResponse?.chat

                    if (apiResponse?.success == true && chat != null) {
                        RefreshManager.notifyChatsChanged()
                        _state.value = _state.value.copy(
                            isCreating = false,
                            createdChat = chat,
                            error = null
                        )
                    } else {
                        _state.value = _state.value.copy(
                            isCreating = false,
                            error = apiResponse?.error ?: "Failed to create chat"
                        )
                    }
                } else {
                    _state.value = _state.value.copy(
                        isCreating = false,
                        error = "HTTP ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isCreating = false,
                    error = "Network error: ${e.message}"
                )
            }
        }
    }

    fun clearCreatedChat() {
        _state.value = _state.value.copy(createdChat = null)
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun reset() {
        _state.value = CreateChatState()
    }

    companion object {
        fun provideFactory(
            apiManager: ApiManager,
            tokenManager: TokenManager
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CreateChatViewModel(apiManager, tokenManager) as T
            }
        }
    }
}