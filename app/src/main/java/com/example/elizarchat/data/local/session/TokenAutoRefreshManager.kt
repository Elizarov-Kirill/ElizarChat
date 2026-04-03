package com.example.elizarchat.data.local.session

import android.content.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class TokenAutoRefreshManager(
    private val context: Context,
    private val tokenManager: TokenManager,
    private val apiManager: com.example.elizarchat.data.remote.ApiManager
) {
    private var refreshJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun startAutoRefresh() {
        refreshJob?.cancel()
        refreshJob = scope.launch {
            while (isActive) {
                delay(60_000) // Проверяем каждую минуту

                val accessExpiry = tokenManager.getTokenInfo().accessTokenExpiresAt?.time ?: 0
                val now = System.currentTimeMillis()
                val timeToExpiry = accessExpiry - now

                // Если до истечения осталось меньше 5 минут - обновляем
                if (timeToExpiry > 0 && timeToExpiry < 5 * 60_000) {
                    println("🔄 Автоматическое обновление токена (истекает через ${timeToExpiry / 1000} сек)")
                    refreshTokenIfNeeded()
                }
            }
        }
    }

    private suspend fun refreshTokenIfNeeded() {
        val refreshToken = tokenManager.getRefreshToken() ?: return
        if (tokenManager.isRefreshTokenExpired()) return

        try {
            val response = apiManager.authApi.refreshToken(
                com.example.elizarchat.data.remote.dto.RefreshTokenRequest(refreshToken)
            )

            if (response.isSuccessful && response.body()?.success == true) {
                val newAccessToken = response.body()?.tokens?.accessToken
                newAccessToken?.let {
                    tokenManager.updateAccessToken(it)
                    println("✅ Токен автоматически обновлен")
                }
            }
        } catch (e: Exception) {
            println("❌ Ошибка автоматического обновления: ${e.message}")
        }
    }

    fun stopAutoRefresh() {
        refreshJob?.cancel()
        refreshJob = null
    }
}