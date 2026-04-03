package com.example.elizarchat.data.remote.config

import android.content.Context
import com.example.elizarchat.data.local.session.TokenManager
import com.example.elizarchat.data.remote.api.AuthApi
import com.example.elizarchat.data.remote.dto.RefreshTokenRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.internal.closeQuietly

class TokenRefreshInterceptor(
    private val context: Context,
    private val tokenManager: TokenManager
) : Interceptor {

    @Volatile
    private var isRefreshing = false

    @Volatile
    private var refreshLock = Any()

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        var response = chain.proceed(originalRequest)

        // Если не 401, просто возвращаем ответ
        if (response.code != 401) {
            return response
        }

        // Закрываем тело ответа
        response.closeQuietly()

        // Проверяем, нужно ли обновлять токен
        val refreshToken = runBlocking { tokenManager.getRefreshToken() }
        if (refreshToken == null || runBlocking { tokenManager.isRefreshTokenExpired() }) {
            println("❌ TokenRefreshInterceptor: Refresh токен отсутствует или истек")
            runBlocking { tokenManager.clearTokens() }
            return response
        }

        // Синхронизируем обновление токена
        synchronized(refreshLock) {
            if (!isRefreshing) {
                isRefreshing = true
                try {
                    val newAccessToken = refreshAccessTokenViaApi(refreshToken)
                    if (newAccessToken != null) {
                        runBlocking { tokenManager.updateAccessToken(newAccessToken) }
                        println("✅ TokenRefreshInterceptor: Токен успешно обновлен")
                    } else {
                        println("❌ TokenRefreshInterceptor: Не удалось обновить токен")
                        runBlocking { tokenManager.clearTokens() }
                        return response
                    }
                } catch (e: Exception) {
                    println("❌ TokenRefreshInterceptor: Ошибка при обновлении токена: ${e.message}")
                    runBlocking { tokenManager.clearTokens() }
                    return response
                } finally {
                    isRefreshing = false
                }
            } else {
                // Ждем пока другой поток обновляет токен
                var retryCount = 0
                while (isRefreshing && retryCount < 20) {
                    try {
                        Thread.sleep(500)
                    } catch (e: InterruptedException) {
                        Thread.currentThread().interrupt()
                        break
                    }
                    retryCount++
                }
                if (retryCount >= 20) {
                    println("❌ TokenRefreshInterceptor: Таймаут ожидания обновления токена")
                    return response
                }
            }
        }

        // Получаем новый токен и повторяем запрос
        val newToken = runBlocking { tokenManager.getAccessToken() }
        return if (newToken != null && newToken != refreshToken) {
            val newRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $newToken")
                .build()
            chain.proceed(newRequest)
        } else {
            response
        }
    }

    private fun refreshAccessTokenViaApi(refreshToken: String): String? {
        return try {
            // Создаем временный клиент без авторизации
            val retrofit = RetrofitConfig.createUnauthenticatedRetrofit()
            val authApi = retrofit.create(AuthApi::class.java)

            val request = RefreshTokenRequest(refreshToken)
            val response = runBlocking { authApi.refreshToken(request) }

            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.tokens?.accessToken
            } else {
                println("❌ TokenRefreshInterceptor: Ошибка API - ${response.code()}")
                null
            }
        } catch (e: Exception) {
            println("❌ TokenRefreshInterceptor: Исключение - ${e.message}")
            null
        }
    }
}