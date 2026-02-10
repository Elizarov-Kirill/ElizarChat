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

                val response = apiManager.getChats(
                    page = _state.value.currentPage,
                    limit = 20
                )

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true) {
                        val chatsResponse = apiResponse.data
                        if (chatsResponse != null) {
                            val currentChats = _state.value.chats
                            val newChats = if (refresh) {
                                chatsResponse.chats
                            } else {
                                currentChats + chatsResponse.chats
                            }

                            _state.value = _state.value.copy(
                                chats = newChats,
                                hasMoreChats = chatsResponse.chats.size >= 20,
                                currentPage = if (refresh) 2 else _state.value.currentPage + 1,
                                isLoading = false,
                                isRefreshing = false,
                                error = null
                            )
                        }
                    } else {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            isRefreshing = false,
                            error = apiResponse?.error ?: "Failed to load chats"
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