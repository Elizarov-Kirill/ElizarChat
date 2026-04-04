package com.example.elizarchat.data.remote.config

import android.content.Context
import com.example.elizarchat.data.local.session.TokenManager
import com.example.elizarchat.data.remote.api.AuthApi
import com.example.elizarchat.data.remote.dto.RefreshTokenRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.internal.closeQuietly
import java.io.IOException

class TokenRefreshInterceptor(
    private val context: Context,
    private val tokenManager: TokenManager
) : Interceptor {

    @Volatile
    private var isRefreshing = false

    @Volatile
    private var refreshLock = Any()

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        var response = chain.proceed(originalRequest)

        // Если не 401, просто возвращаем ответ
        if (response.code != 401) {
            return response
        }

        println("🔄 TokenRefreshInterceptor: Получен 401, пробуем обновить токен")

        // ВАЖНО: Закрываем response, чтобы освободить ресурсы
        response.closeQuietly()

        // Проверяем, нужно ли обновлять токен
        val refreshToken = runBlocking { tokenManager.getRefreshToken() }
        if (refreshToken == null || runBlocking { tokenManager.isRefreshTokenExpired() }) {
            println("❌ TokenRefreshInterceptor: Refresh токен отсутствует или истек")
            runBlocking { tokenManager.clearTokens() }
            // Возвращаем новый response с ошибкой 401
            return response
        }

        // Синхронизируем обновление токена
        var newAccessToken: String? = null

        synchronized(refreshLock) {
            if (!isRefreshing) {
                isRefreshing = true
                try {
                    newAccessToken = refreshAccessTokenViaApi(refreshToken)
                    if (newAccessToken != null) {
                        runBlocking { tokenManager.updateAccessToken(newAccessToken!!) }
                        println("✅ TokenRefreshInterceptor: Токен успешно обновлен")
                    } else {
                        println("❌ TokenRefreshInterceptor: Не удалось обновить токен")
                        runBlocking { tokenManager.clearTokens() }
                        return response
                    }
                } catch (e: Exception) {
                    println("❌ TokenRefreshInterceptor: Ошибка при обновлении токена: ${e.message}")
                    e.printStackTrace()
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

                // Получаем обновленный токен
                newAccessToken = runBlocking { tokenManager.getAccessToken() }
                if (newAccessToken == null || runBlocking { tokenManager.isAccessTokenExpired() }) {
                    println("❌ TokenRefreshInterceptor: Токен не был обновлен после ожидания")
                    return response
                }
            }
        }

        // Получаем новый токен и повторяем запрос
        val finalToken = newAccessToken ?: runBlocking { tokenManager.getAccessToken() }

        return if (finalToken != null && !runBlocking { tokenManager.isAccessTokenExpired() }) {
            println("🔄 TokenRefreshInterceptor: Повторяем запрос с новым токеном")

            // Создаем новый запрос с обновленным токеном
            val newRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $finalToken")
                .build()

            // Повторяем запрос
            try {
                chain.proceed(newRequest)
            } catch (e: Exception) {
                println("❌ TokenRefreshInterceptor: Ошибка при повторном запросе: ${e.message}")
                throw e
            }
        } else {
            println("❌ TokenRefreshInterceptor: Новый токен не получен или истек")
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

            if (response.isSuccessful) {
                val body = response.body()
                println("✅ TokenRefreshInterceptor: Response body: $body")

                // Пробуем получить accessToken из разных форматов ответа
                val accessToken = when {
                    body?.accessToken != null -> {
                        println("✅ Получен accessToken из поля accessToken")
                        body.accessToken
                    }
                    body?.tokens?.accessToken != null -> {
                        println("✅ Получен accessToken из tokens.accessToken")
                        body.tokens.accessToken
                    }
                    else -> {
                        println("❌ Неизвестный формат ответа: $body")
                        null
                    }
                }

                return accessToken
            } else {
                val errorBody = response.errorBody()?.string()
                println("❌ TokenRefreshInterceptor: Ошибка API - ${response.code()}, body: $errorBody")
                null
            }
        } catch (e: Exception) {
            println("❌ TokenRefreshInterceptor: Исключение - ${e.message}")
            e.printStackTrace()
            null
        }
    }
}