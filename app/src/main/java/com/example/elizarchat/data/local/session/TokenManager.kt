package com.example.elizarchat.data.local.session

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Менеджер для работы с токенами, предоставляющий LiveData для UI слоя
 */
class TokenManager(private val context: Context) {

    private val tokenStorage = TokenStorage(context)
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    init {
        // Инициализируем состояние авторизации
        coroutineScope.launch {
            tokenStorage.isLoggedInFlow.collect { isLoggedIn ->
                _authState.postValue(
                    if (isLoggedIn) AuthState.AUTHENTICATED else AuthState.UNAUTHENTICATED
                )
            }
        }
    }

    suspend fun saveTokens(
        accessToken: String,
        refreshToken: String,
        userId: String? = null
    ) {
        tokenStorage.saveTokens(accessToken, refreshToken, userId)
    }

    suspend fun getAccessToken(): String? = tokenStorage.getAccessToken()
    suspend fun getRefreshToken(): String? = tokenStorage.getRefreshToken()
    suspend fun getUserId(): String? = tokenStorage.getUserId()

    suspend fun isLoggedIn(): Boolean = tokenStorage.isLoggedIn()


    suspend fun clearTokens() {
        tokenStorage.clearTokens()
    }

    suspend fun shouldRefreshToken(): String? = tokenStorage.shouldRefreshToken()

    suspend fun updateAccessToken(newAccessToken: String) {
        tokenStorage.updateAccessToken(newAccessToken)
    }

    suspend fun isAccessTokenExpired(): Boolean = tokenStorage.isAccessTokenExpired()

    suspend fun isRefreshTokenExpired(): Boolean = tokenStorage.isRefreshTokenExpired()

    suspend fun getTokenInfo(): TokenStorage.TokenInfo = tokenStorage.getTokenInfo()

    // Не-suspend методы для UI
    fun getAccessTokenSync(): String? = tokenStorage.getAccessTokenSync()
    fun getRefreshTokenSync(): String? = tokenStorage.getRefreshTokenSync()
    fun getUserIdSync(): String? = runBlocking { tokenStorage.getUserId() }
    fun isLoggedInSync(): Boolean = tokenStorage.isLoggedInSync()

    // LiveData для UI
    val isLoggedInLiveData = tokenStorage.isLoggedInFlow.asLiveData()
    val userIdLiveData = tokenStorage.userIdFlow.asLiveData()

    sealed class AuthState {
        object AUTHENTICATED : AuthState()
        object UNAUTHENTICATED : AuthState()
        data class ERROR(val message: String) : AuthState()
    }

    companion object {
        @Volatile
        private var INSTANCE: TokenManager? = null

        fun getInstance(context: Context): TokenManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TokenManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}