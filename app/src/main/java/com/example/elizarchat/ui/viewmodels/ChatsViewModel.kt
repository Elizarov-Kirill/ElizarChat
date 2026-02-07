package com.example.elizarchat.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.elizarchat.data.local.session.TokenManager
import com.example.elizarchat.data.remote.ApiManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ChatsState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false
)

class ChatsViewModel(
    private val apiManager: ApiManager,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(ChatsState())
    val state: StateFlow<ChatsState> = _state

    init {
        println("🚀 DEBUG ChatsViewModel: Инициализация")
        checkAuth()
    }

    fun checkAuth() {
        viewModelScope.launch {
            println("🔐 DEBUG ChatsViewModel.checkAuth(): Проверка авторизации")
            _state.value = _state.value.copy(isLoading = true)

            try {
                val isLoggedIn = tokenManager.isLoggedIn()
                println("🔐 DEBUG ChatsViewModel.checkAuth(): isLoggedIn = $isLoggedIn")

                _state.value = _state.value.copy(
                    isLoading = false,
                    isLoggedIn = isLoggedIn
                )
            } catch (e: Exception) {
                println("❌ DEBUG ChatsViewModel.checkAuth(): Ошибка: ${e.message}")
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Auth check failed: ${e.message}"
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            println("🚪 DEBUG ChatsViewModel.logout(): Выход")
            tokenManager.clearTokens()
            _state.value = _state.value.copy(isLoggedIn = false)
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