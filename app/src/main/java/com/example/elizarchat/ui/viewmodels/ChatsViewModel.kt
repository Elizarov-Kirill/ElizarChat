package com.example.elizarchat.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.elizarchat.data.local.session.TokenManager
import com.example.elizarchat.data.remote.ApiManager
import com.example.elizarchat.data.remote.dto.ChatDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatsState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val chats: List<ChatDto> = emptyList(),
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

    init {
        loadChats()
    }

    fun loadChats(refresh: Boolean = false) {
        viewModelScope.launch {
            try {
                if (refresh) {
                    _state.value = _state.value.copy(
                        isRefreshing = true,
                        currentPage = 1,
                        hasMoreChats = true,
                        chats = emptyList()
                    )
                } else {
                    _state.value = _state.value.copy(isLoading = true)
                }

                println("📡 Загрузка чатов, страница: ${_state.value.currentPage}")

                val response = apiManager.getChats(
                    page = _state.value.currentPage,
                    limit = 20
                )

                if (response.isSuccessful) {
                    val apiResponse = response.body()

                    // ИСПРАВЛЕНО: Получаем часы из поля chats
                    val chats = apiResponse?.chats ?: emptyList()

                    println("✅ Получено чатов: ${chats.size}")
                    chats.forEach { chat ->
                        println("   - Чат ID=${chat.id}, name=${chat.name}, type=${chat.type}")
                    }

                    val currentChats = if (refresh) emptyList() else _state.value.chats
                    val newChats = currentChats + chats

                    // Проверяем, есть ли еще чаты
                    val hasMore = chats.size >= 20
                    val nextPage = if (refresh) 2 else _state.value.currentPage + 1

                    _state.value = _state.value.copy(
                        chats = newChats,
                        hasMoreChats = hasMore,
                        currentPage = nextPage,
                        isLoading = false,
                        isRefreshing = false,
                        error = null
                    )

                    println("📊 Всего чатов в состоянии: ${_state.value.chats.size}")
                } else {
                    println("❌ HTTP ошибка: ${response.code()}")
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = "HTTP ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                println("❌ Исключение: ${e.message}")
                e.printStackTrace()
                _state.value = _state.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    error = "Network error: ${e.message}"
                )
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
        _state.value = _state.value.copy(error = null)
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