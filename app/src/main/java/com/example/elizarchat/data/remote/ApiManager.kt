package com.example.elizarchat.data.remote

import android.content.Context
import com.example.elizarchat.data.local.session.TokenManager
import com.example.elizarchat.data.remote.api.*
import com.example.elizarchat.data.remote.config.RetrofitConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class ApiManager(context: Context) {
    private val tokenManager = TokenManager.getInstance(context)

    // Провайдер токена для интерцептора
    private val tokenProvider: suspend () -> String? = {
        tokenManager.getAccessToken()
    }

    // Retrofit экземпляры
    private val unauthenticatedRetrofit = RetrofitConfig.createUnauthenticatedRetrofit()
    private val authenticatedRetrofit = RetrofitConfig.createAuthenticatedRetrofit(
        { runBlocking { tokenManager.getAccessToken() } }
    )

    // API интерфейсы
    val authApi: AuthApi by lazy {
        unauthenticatedRetrofit.create(AuthApi::class.java)
    }

    val systemApi: SystemApi by lazy {
        unauthenticatedRetrofit.create(SystemApi::class.java)
    }

    val userApi: UserApi by lazy {
        authenticatedRetrofit.create(UserApi::class.java)
    }

    val chatApi: ChatApi by lazy {
        authenticatedRetrofit.create(ChatApi::class.java)
    }

    val messageApi: MessageApi by lazy {
        authenticatedRetrofit.create(MessageApi::class.java)
    }

    /**
     * Safe API call с автоматическим refresh токенов
     */
    suspend fun <T> safeApiCall(
        apiCall: suspend () -> Response<T>,
        maxRetries: Int = 1
    ): Result<T> {
        var retryCount = 0

        while (retryCount <= maxRetries) {
            try {
                val response = apiCall()

                when {
                    response.isSuccessful -> {
                        val body = response.body()
                        return if (body != null) {
                            Result.success(body)
                        } else {
                            Result.failure(Exception("Response body is null"))
                        }
                    }

                    response.code() == 401 && retryCount < maxRetries -> {
                        // Пробуем обновить токен при 401 ошибке
                        if (refreshAccessToken()) {
                            retryCount++
                            continue // Повторяем запрос с новым токеном
                        } else {
                            // Не удалось обновить - делаем логаут
                            tokenManager.clearTokens()
                            return Result.failure(Exception("Authentication failed"))
                        }
                    }

                    else -> {
                        val errorBody = response.errorBody()?.string()
                        return Result.failure(
                            Exception("HTTP ${response.code()}: ${errorBody ?: response.message()}")
                        )
                    }
                }
            } catch (e: IOException) {
                return Result.failure(Exception("Network error: ${e.message}"))
            } catch (e: HttpException) {
                return Result.failure(Exception("HTTP error: ${e.message}"))
            } catch (e: Exception) {
                return Result.failure(Exception("Unknown error: ${e.message}"))
            }
        }

        return Result.failure(Exception("Max retries exceeded"))
    }

    /**
     * Обновление access токена через refresh токен
     */
    suspend fun refreshAccessToken(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val refreshToken = tokenManager.getRefreshToken() ?: return@withContext false

                // Используем RefreshTokenRequest
                val request = com.example.elizarchat.data.remote.dto.RefreshTokenRequest(
                    refreshToken = refreshToken
                )

                val response = authApi.refreshToken(request)

                if (response.isSuccessful) {
                    // ВАЖНО: authApi.refreshToken() возвращает AuthResponse напрямую!
                    val authResponse = response.body()

                    if (authResponse?.success == true) {
                        // Сохраняем новые токены через TokenManager
                        tokenManager.saveTokens(
                            authResponse.tokens.accessToken,
                            authResponse.tokens.refreshToken,
                            authResponse.user.id.toString()
                        )
                        true
                    } else {
                        false
                    }
                } else {
                    false
                }
            } catch (e: Exception) {
                println("❌ DEBUG ApiManager.refreshAccessToken(): Ошибка: ${e.message}")
                false
            }
        }
    }
}