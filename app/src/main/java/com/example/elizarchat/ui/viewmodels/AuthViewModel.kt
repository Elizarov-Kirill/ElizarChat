// 📁 ui/viewmodels/AuthViewModel.kt - ОБНОВЛЕННЫЙ
package com.example.elizarchat.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.elizarchat.data.local.session.TokenManager
import com.example.elizarchat.data.remote.ApiManager
import com.example.elizarchat.data.remote.dto.LoginRequest
import com.example.elizarchat.data.remote.dto.RegisterRequest
import com.example.elizarchat.data.remote.websocket.WebSocketManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val displayName: String = "",
    val isLoginSuccessful: Boolean = false,
    val isRegisterSuccessful: Boolean = false
)

class AuthViewModel(
    private val apiManager: ApiManager,
    private val tokenManager: TokenManager,
    private val webSocketManager: WebSocketManager // ДОБАВЛЕНО
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    fun updateUsername(username: String) {
        _state.value = _state.value.copy(username = username)
    }

    fun updateEmail(email: String) {
        _state.value = _state.value.copy(email = email)
    }

    fun updatePassword(password: String) {
        _state.value = _state.value.copy(password = password)
    }

    fun updateDisplayName(displayName: String) {
        _state.value = _state.value.copy(displayName = displayName)
    }

    fun register() {
        val username = _state.value.username
        val email = _state.value.email
        val password = _state.value.password
        val displayName = _state.value.displayName

        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            _state.value = _state.value.copy(
                error = "Заполните все обязательные поля"
            )
            return
        }

        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(
                    isLoading = true,
                    error = null
                )

                val request = RegisterRequest(
                    username = username,
                    email = email,
                    password = password,
                    displayName = if (displayName.isNotBlank()) displayName else null
                )

                val response = apiManager.authApi.register(request)

                if (response.isSuccessful) {
                    val authResponse = response.body()
                    if (authResponse?.success == true) {
                        // Сохраняем токены
                        authResponse.tokens.let { tokens ->
                            tokenManager.saveTokens(
                                accessToken = tokens.accessToken,
                                refreshToken = tokens.refreshToken,
                                userId = authResponse.user.id.toString()
                            )
                        }

                        // ЗАПУСКАЕМ WEBSOCKET ПОСЛЕ РЕГИСТРАЦИИ
                        launchWebSocketConnection()

                        _state.value = _state.value.copy(
                            isLoading = false,
                            isRegisterSuccessful = true,
                            error = null
                        )
                    } else {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = authResponse?.error ?: "Ошибка регистрации"
                        )
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "HTTP ${response.code()}: $errorBody"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Ошибка сети: ${e.message}"
                )
            }
        }
    }

    fun login() {
        val email = _state.value.email
        val password = _state.value.password

        if (email.isBlank() || password.isBlank()) {
            _state.value = _state.value.copy(
                error = "Введите email и пароль"
            )
            return
        }

        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(
                    isLoading = true,
                    error = null
                )

                val request = LoginRequest(
                    email = email,
                    password = password
                )

                val response = apiManager.authApi.login(request)

                if (response.isSuccessful) {
                    val authResponse = response.body()
                    if (authResponse?.success == true) {
                        // Сохраняем токены
                        authResponse.tokens.let { tokens ->
                            tokenManager.saveTokens(
                                accessToken = tokens.accessToken,
                                refreshToken = tokens.refreshToken,
                                userId = authResponse.user.id.toString()
                            )
                        }

                        // ЗАПУСКАЕМ WEBSOCKET ПОСЛЕ ВХОДА
                        launchWebSocketConnection()

                        _state.value = _state.value.copy(
                            isLoading = false,
                            isLoginSuccessful = true,
                            error = null
                        )
                    } else {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = authResponse?.error ?: "Ошибка входа"
                        )
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "HTTP ${response.code()}: $errorBody"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Ошибка сети: ${e.message}"
                )
            }
        }
    }

    private fun launchWebSocketConnection() {
        viewModelScope.launch {
            println("🚀 AuthViewModel: Запуск WebSocket после успешной аутентификации")
            webSocketManager.connect()

            // Ждем подключения (максимум 5 секунд)
            repeat(50) { // 50 итераций по 100мс = 5 секунд
                if (webSocketManager.isConnected()) {
                    println("✅ AuthViewModel: WebSocket успешно подключен")
                    return@launch
                }
                kotlinx.coroutines.delay(100)
            }
            println("⚠️ AuthViewModel: WebSocket не подключился в течение 5 секунд")
        }
    }

    fun resetSuccessStates() {
        _state.value = _state.value.copy(
            isLoginSuccessful = false,
            isRegisterSuccessful = false
        )
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun reset() {
        _state.value = AuthState()
    }

    companion object {

    }
}