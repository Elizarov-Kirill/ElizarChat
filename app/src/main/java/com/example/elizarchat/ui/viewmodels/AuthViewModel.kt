package com.example.elizarchat.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.elizarchat.data.remote.ApiManager
import com.example.elizarchat.data.local.session.TokenManager
import com.example.elizarchat.data.remote.dto.LoginRequest
import com.example.elizarchat.data.remote.dto.RegisterRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthState(
    val email: String = "",
    val password: String = "",
    val username: String = "",
    val displayName: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoginSuccessful: Boolean = false,
    val isRegisterSuccessful: Boolean = false
)

class AuthViewModel(
    private val apiManager: ApiManager,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state

    fun updateEmail(email: String) {
        _state.value = _state.value.copy(email = email)
    }

    fun updatePassword(password: String) {
        _state.value = _state.value.copy(password = password)
    }

    fun updateUsername(username: String) {
        _state.value = _state.value.copy(username = username)
    }

    fun updateDisplayName(displayName: String) {
        _state.value = _state.value.copy(displayName = displayName)
    }

    // AuthViewModel.kt - добавьте логи в методы login() и register()
    fun login() {
        viewModelScope.launch {
            println("🔐 DEBUG AuthViewModel.login(): Начало логина")
            _state.value = _state.value.copy(isLoading = true, error = null)

            try {
                val request = LoginRequest(
                    email = _state.value.email,
                    password = _state.value.password
                )

                println("📤 DEBUG AuthViewModel.login(): Отправка запроса с email: ${_state.value.email}")

                val response = apiManager.authApi.login(request)

                println("📥 DEBUG AuthViewModel.login(): Ответ получен: ${response.isSuccessful}, код: ${response.code()}")

                if (response.isSuccessful) {
                    // ВАЖНО: Теперь получаем AuthResponse напрямую, не ApiResponse<AuthData>
                    val authResponse = response.body()
                    println("✅ DEBUG AuthViewModel.login(): AuthResponse получен: $authResponse")

                    if (authResponse?.success == true) {
                        // Сохраняем токены
                        println("💾 DEBUG AuthViewModel.login(): Сохранение токенов, userId: ${authResponse.user.id}")

                        tokenManager.saveTokens(
                            authResponse.tokens.accessToken,
                            authResponse.tokens.refreshToken,
                            authResponse.user.id.toString()
                        )

                        // Проверяем что токены сохранились
                        val savedAccessToken = tokenManager.getAccessToken()
                        val savedUserId = tokenManager.getUserId()

                        println("🔍 DEBUG AuthViewModel.login(): Проверка после сохранения:")
                        println("🔍 DEBUG AuthViewModel.login(): - accessToken сохранен: ${savedAccessToken != null}")
                        println("🔍 DEBUG AuthViewModel.login(): - userId сохранен: $savedUserId")

                        _state.value = _state.value.copy(
                            isLoading = false,
                            isLoginSuccessful = true,
                            error = null
                        )
                        println("🎉 DEBUG AuthViewModel.login(): Установлен isLoginSuccessful = true")
                    } else {
                        println("❌ DEBUG AuthViewModel.login(): Логин неуспешен: ${authResponse?.error}")
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = authResponse?.error ?: "Login failed"
                        )
                    }
                } else {
                    println("❌ DEBUG AuthViewModel.login(): HTTP ошибка: ${response.code()}")
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "HTTP ${response.code()}: ${response.message()}"
                    )
                }
            } catch (e: Exception) {
                println("💥 DEBUG AuthViewModel.login(): Исключение: ${e.message}")
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Error: ${e.message}"
                )
            }
        }
    }

    fun register() {
        viewModelScope.launch {
            println("🔐 DEBUG AuthViewModel.register(): Начало регистрации")
            _state.value = _state.value.copy(isLoading = true, error = null)

            try {
                val request = RegisterRequest(
                    username = _state.value.username,
                    email = _state.value.email,
                    password = _state.value.password,
                    displayName = _state.value.displayName
                )

                println("📤 DEBUG AuthViewModel.register(): Отправка запроса регистрации")
                println("📤 DEBUG AuthViewModel.register(): Данные: username=${request.username}, email=${request.email}")

                val response = apiManager.authApi.register(request)

                println("📥 DEBUG AuthViewModel.register(): Ответ получен: ${response.isSuccessful}, код: ${response.code()}")

                if (response.isSuccessful) {
                    // ВАЖНО: authApi.register() тоже возвращает AuthResponse напрямую!
                    val authResponse = response.body()
                    println("✅ DEBUG AuthViewModel.register(): AuthResponse получен: $authResponse")

                    if (authResponse?.success == true) {
                        // Сохраняем токены
                        println("💾 DEBUG AuthViewModel.register(): Сохранение токенов, userId: ${authResponse.user.id}")

                        tokenManager.saveTokens(
                            authResponse.tokens.accessToken,
                            authResponse.tokens.refreshToken,
                            authResponse.user.id.toString()
                        )

                        _state.value = _state.value.copy(
                            isLoading = false,
                            isRegisterSuccessful = true,
                            error = null
                        )
                        println("🎉 DEBUG AuthViewModel.register(): Установлен isRegisterSuccessful = true")
                    } else {
                        println("❌ DEBUG AuthViewModel.register(): Регистрация неуспешна: ${authResponse?.error}")
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = authResponse?.error ?: "Registration failed"
                        )
                    }
                } else {
                    println("❌ DEBUG AuthViewModel.register(): HTTP ошибка: ${response.code()}")
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "HTTP ${response.code()}: ${response.message()}"
                    )
                }
            } catch (e: Exception) {
                println("💥 DEBUG AuthViewModel.register(): Исключение: ${e.message}")
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Error: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun resetSuccessStates() {
        _state.value = _state.value.copy(
            isLoginSuccessful = false,
            isRegisterSuccessful = false
        )
    }

    companion object {
        fun provideFactory(
            apiManager: ApiManager,
            tokenManager: TokenManager
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AuthViewModel(apiManager, tokenManager) as T
            }
        }
    }
}