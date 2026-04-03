package com.example.elizarchat.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.elizarchat.data.local.RefreshManager
import com.example.elizarchat.data.local.session.TokenManager
import com.example.elizarchat.data.remote.ApiManager
import com.example.elizarchat.data.remote.dto.CreateChatRequest
import com.example.elizarchat.data.remote.dto.UserDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

data class NewChatState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val users: List<UserDto> = emptyList(),
    val isSearching: Boolean = false,
    val isCreatingChat: Boolean = false,
    val creatingChatMessage: String = ""
)

data class GroupChatState(
    val selectedUsers: List<UserDto> = emptyList(),
    val chatName: String = "",
    val isCreating: Boolean = false,
    val error: String? = null
)

class NewChatViewModel(
    private val apiManager: ApiManager,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(NewChatState())
    val state: StateFlow<NewChatState> = _state.asStateFlow()

    private val _groupChatState = MutableStateFlow(GroupChatState())
    val groupChatState: StateFlow<GroupChatState> = _groupChatState.asStateFlow()

    private val _navigationTarget = MutableStateFlow<Int?>(null)
    val navigationTarget: StateFlow<Int?> = _navigationTarget.asStateFlow()

    private val currentUserId: Int = runBlocking {
        tokenManager.getUserId()?.toIntOrNull() ?: 0
    }

    // ============ ПОИСК ПОЛЬЗОВАТЕЛЕЙ ============

    fun updateSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
        if (query.length >= 2) {
            searchUsers()
        } else {
            _state.update { it.copy(users = emptyList()) }
        }
    }

    private fun searchUsers() {
        val query = _state.value.searchQuery
        if (query.length < 2) return

        viewModelScope.launch {
            try {
                _state.update { it.copy(isSearching = true, error = null) }

                val response = apiManager.searchUsers(query = query)

                if (response.isSuccessful) {
                    val usersResponse = response.body()
                    val users = usersResponse?.users ?: emptyList()
                    val filteredUsers = users.filter { it.id != currentUserId }
                    _state.update {
                        it.copy(
                            users = filteredUsers,
                            isSearching = false
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            error = "HTTP ${response.code()}: ${response.message()}",
                            isSearching = false
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        error = "Network error: ${e.message}",
                        isSearching = false
                    )
                }
            }
        }
    }

    // ============ ПРИВАТНЫЙ ЧАТ ============

    fun startPrivateChat(userId: Int) {
        viewModelScope.launch {
            try {
                _state.update {
                    it.copy(
                        isCreatingChat = true,
                        creatingChatMessage = "Поиск или создание чата..."
                    )
                }

                val existingChatId = findExistingPrivateChat(userId)

                if (existingChatId != null) {
                    println("✅ Найден существующий приватный чат: $existingChatId")
                    _state.update {
                        it.copy(
                            isCreatingChat = false,
                            creatingChatMessage = ""
                        )
                    }
                    _navigationTarget.value = existingChatId
                    return@launch
                }

                _state.update {
                    it.copy(creatingChatMessage = "Создание нового чата...")
                }

                val newChatId = createNewPrivateChat(userId)

                if (newChatId != null) {
                    println("✅ Создан новый приватный чат: $newChatId")
                    RefreshManager.notifyChatsChanged()
                    _state.update {
                        it.copy(
                            isCreatingChat = false,
                            creatingChatMessage = ""
                        )
                    }
                    _navigationTarget.value = newChatId
                } else {
                    _state.update {
                        it.copy(
                            isCreatingChat = false,
                            creatingChatMessage = "",
                            error = "Не удалось создать чат"
                        )
                    }
                }
            } catch (e: Exception) {
                println("❌ Ошибка: ${e.message}")
                _state.update {
                    it.copy(
                        isCreatingChat = false,
                        creatingChatMessage = "",
                        error = "Ошибка: ${e.message}"
                    )
                }
            }
        }
    }

    private suspend fun findExistingPrivateChat(userId: Int): Int? {
        return try {
            val response = apiManager.getPrivateChatWithUser(userId)

            if (response.isSuccessful) {
                val apiResponse = response.body()
                val existingChat = apiResponse?.chat

                if (existingChat != null && apiResponse.success) {
                    return existingChat.id
                }
            }
            null
        } catch (e: Exception) {
            println("❌ Ошибка при поиске чата: ${e.message}")
            null
        }
    }

    private suspend fun createNewPrivateChat(userId: Int): Int? {
        return try {
            val sortedIds = listOf(currentUserId, userId).sorted()
            val chatName = "private_${sortedIds[0]}_${sortedIds[1]}"

            val request = CreateChatRequest(
                type = "private",
                name = chatName,
                userIds = listOf(userId)
            )

            val response = apiManager.createChat(request)

            if (response.isSuccessful) {
                val apiResponse = response.body()
                val newChat = apiResponse?.chat

                if (apiResponse?.success == true && newChat != null) {
                    return newChat.id
                }
            }

            null
        } catch (e: Exception) {
            println("❌ Ошибка создания чата: ${e.message}")
            null
        }
    }

    // ============ ГРУППОВОЙ ЧАТ ============

    fun addUserToGroup(user: UserDto) {
        val current = _groupChatState.value.selectedUsers
        if (current.none { it.id == user.id }) {
            _groupChatState.update {
                it.copy(selectedUsers = current + user, error = null)
            }
        }
    }

    fun removeUserFromGroup(userId: Int) {
        _groupChatState.update {
            it.copy(
                selectedUsers = it.selectedUsers.filter { user -> user.id != userId },
                error = null
            )
        }
    }

    fun updateGroupChatName(name: String) {
        _groupChatState.update { it.copy(chatName = name, error = null) }
    }

    fun resetGroupChat() {
        _groupChatState.update {
            GroupChatState()
        }
    }

    fun createGroupChat() {
        val selectedUsers = _groupChatState.value.selectedUsers
        val chatName = _groupChatState.value.chatName

        when {
            selectedUsers.size < 2 -> {
                _groupChatState.update {
                    it.copy(error = "Выберите минимум 2 участника")
                }
                return
            }
            chatName.isBlank() -> {
                _groupChatState.update {
                    it.copy(error = "Введите название группы")
                }
                return
            }
        }

        viewModelScope.launch {
            try {
                _groupChatState.update { it.copy(isCreating = true, error = null) }

                val userIds = selectedUsers.map { it.id }

                val request = CreateChatRequest(
                    type = "group",
                    name = chatName,
                    userIds = userIds
                )

                val response = apiManager.createChat(request)

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    val newChat = apiResponse?.chat

                    if (apiResponse?.success == true && newChat != null) {
                        println("✅ Создан групповой чат: ${newChat.id}")
                        RefreshManager.notifyChatsChanged()
                        _groupChatState.update {
                            it.copy(isCreating = false)
                        }
                        _navigationTarget.value = newChat.id
                    } else {
                        _groupChatState.update {
                            it.copy(
                                isCreating = false,
                                error = apiResponse?.error ?: "Не удалось создать групповой чат"
                            )
                        }
                    }
                } else {
                    _groupChatState.update {
                        it.copy(
                            isCreating = false,
                            error = "HTTP ${response.code()}: ${response.message()}"
                        )
                    }
                }
            } catch (e: Exception) {
                println("❌ Ошибка создания группового чата: ${e.message}")
                _groupChatState.update {
                    it.copy(
                        isCreating = false,
                        error = "Ошибка: ${e.message}"
                    )
                }
            }
        }
    }

    // ============ ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ============

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun clearNavigationTarget() {
        _navigationTarget.value = null
    }

    companion object {
        fun provideFactory(
            apiManager: ApiManager,
            tokenManager: TokenManager
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return NewChatViewModel(apiManager, tokenManager) as T
            }
        }
    }
}