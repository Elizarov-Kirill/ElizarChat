package com.example.elizarchat.data.remote

import android.content.Context
import com.example.elizarchat.data.local.session.TokenManager
import com.example.elizarchat.data.remote.api.*
import com.example.elizarchat.data.remote.config.RetrofitConfig
import com.example.elizarchat.data.remote.dto.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class ApiManager private constructor(
    private val context: Context,
    private val tokenManager: TokenManager
) {
    companion object {
        @Volatile
        private var INSTANCE: ApiManager? = null

        fun getInstance(context: Context, tokenManager: TokenManager): ApiManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ApiManager(context.applicationContext, tokenManager).also {
                    INSTANCE = it
                }
            }
        }

        // Для неавторизованных запросов (только authApi)
        fun getUnauthenticatedInstance(context: Context): ApiManager {
            val tempTokenManager = TokenManager.getInstance(context)
            return ApiManager(context, tempTokenManager)
        }
    }

    // Мьютекс для синхронизации обновления токена
    private val refreshMutex = Mutex()
    private var isRefreshing = false

    // Провайдер токена для интерцептора (синхронный для OkHttp)
    private val tokenProvider: () -> String? = {
        runBlocking { tokenManager.getAccessToken() }
    }

    // Retrofit экземпляры
    private val unauthenticatedRetrofit = RetrofitConfig.createUnauthenticatedRetrofit()

    // Используем обновленный Retrofit с поддержкой refresh токена
    private val authenticatedRetrofit = RetrofitConfig.createAuthenticatedRetrofitWithRefresh(
        context,
        tokenProvider,
        tokenManager
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
     * Используется для случаев, когда нужно дополнительное управление
     */
    suspend fun <T> safeApiCall(
        apiCall: suspend () -> Response<T>,
        maxRetries: Int = 2
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
                        println("🔄 API Call: Получен 401, пробуем обновить токен (попытка ${retryCount + 1})")

                        if (refreshAccessToken()) {
                            retryCount++
                            delay(100)
                            continue
                        } else {
                            println("❌ API Call: Не удалось обновить токен, делаем логаут")
                            tokenManager.clearTokens()
                            return Result.failure(Exception("Authentication failed - please login again"))
                        }
                    }

                    else -> {
                        val errorBody = response.errorBody()?.string()
                        val errorMessage = when (response.code()) {
                            400 -> "Bad request: $errorBody"
                            403 -> "Access forbidden"
                            404 -> "Resource not found"
                            500 -> "Server error"
                            else -> "HTTP ${response.code()}: ${errorBody ?: response.message()}"
                        }
                        return Result.failure(Exception(errorMessage))
                    }
                }
            } catch (e: IOException) {
                println("❌ Network error: ${e.message}")
                return Result.failure(Exception("Network error: ${e.message}"))
            } catch (e: HttpException) {
                println("❌ HTTP error: ${e.message}")
                return Result.failure(Exception("HTTP error: ${e.message}"))
            } catch (e: Exception) {
                println("❌ Unknown error: ${e.message}")
                e.printStackTrace()
                return Result.failure(Exception("Error: ${e.message}"))
            }
        }

        return Result.failure(Exception("Max retries exceeded"))
    }

    /**
     * Обновление access токена через refresh токен
     */
    suspend fun refreshAccessToken(): Boolean {
        return refreshMutex.withLock {
            if (isRefreshing) {
                println("⏳ Обновление токена уже выполняется, ожидаем...")
                var attempts = 0
                while (isRefreshing && attempts < 30) {
                    delay(200)
                    attempts++
                }
                return@withLock if (!tokenManager.isAccessTokenExpired()) {
                    println("✅ Токен успешно обновлен в другом потоке")
                    true
                } else {
                    println("❌ Токен не был обновлен после ожидания")
                    false
                }
            }

            isRefreshing = true
            try {
                println("🔄 Начинаем обновление access токена...")

                val refreshToken = tokenManager.getRefreshToken()
                if (refreshToken == null) {
                    println("❌ Refresh токен отсутствует")
                    return@withLock false
                }

                if (tokenManager.isRefreshTokenExpired()) {
                    println("❌ Refresh токен истек")
                    tokenManager.clearTokens()
                    return@withLock false
                }

                val request = RefreshTokenRequest(refreshToken = refreshToken)
                val response = authApi.refreshToken(request)

                if (response.isSuccessful) {
                    val authResponse = response.body()

                    if (authResponse?.success == true) {
                        tokenManager.saveTokens(
                            authResponse.tokens.accessToken,
                            authResponse.tokens.refreshToken,
                            authResponse.user.id.toString()
                        )
                        println("✅ Access токен успешно обновлен")
                        true
                    } else {
                        println("❌ Ошибка в ответе сервера: ${authResponse?.error}")
                        tokenManager.clearTokens()
                        false
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    println("❌ Ошибка обновления токена: HTTP ${response.code()} - $errorBody")

                    if (response.code() == 401 || response.code() == 400) {
                        tokenManager.clearTokens()
                    }
                    false
                }
            } catch (e: Exception) {
                println("❌ Исключение при обновлении токена: ${e.message}")
                e.printStackTrace()
                false
            } finally {
                isRefreshing = false
            }
        }
    }

    /**
     * Проверяет, нужно ли обновить токен
     */
    suspend fun checkAndRefreshTokenIfNeeded(): Boolean {
        return if (tokenManager.shouldRefreshToken() != null) {
            refreshAccessToken()
        } else {
            true
        }
    }

    // ============ ЧАТЫ ============

    suspend fun getChats(page: Int = 1, limit: Int = 20): Response<ApiResponse<ChatsResponse>> {
        return chatApi.getChats(page, limit)
    }

    suspend fun getChatById(id: Int): Response<ApiResponse<ChatDto>> {
        return chatApi.getChatById(id)
    }

    suspend fun getPrivateChatWithUser(userId: Int): Response<ApiResponse<ChatDto>> {
        return chatApi.getPrivateChatWithUser(userId)
    }

    suspend fun createChat(request: CreateChatRequest): Response<CreateChatResponse> {
        return chatApi.createChat(request)
    }

    // Упрощенный метод для создания чата (без safeApiCall)
    suspend fun createChatAndGetChat(request: CreateChatRequest): Result<ChatDto> {
        return try {
            val response = createChat(request)
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true && apiResponse.chat != null) {
                    Result.success(apiResponse.chat)
                } else {
                    Result.failure(Exception(apiResponse?.error ?: "Failed to create chat"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("HTTP ${response.code()}: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ============ СООБЩЕНИЯ ============

    suspend fun getMessages(
        chatId: Int,
        limit: Int = 50,
        offset: Int = 0,
        before: String? = null
    ): Response<ApiResponse<MessagesResponse>> {
        return messageApi.getMessages(chatId, limit, offset, before)
    }

    suspend fun sendMessage(
        chatId: Int,
        request: SendMessageRequest
    ): Response<ApiResponse<MessageDto>> {
        return messageApi.sendMessage(chatId, request)
    }

    suspend fun sendTextMessage(
        chatId: Int,
        content: String,
        replyTo: Int? = null
    ): Response<ApiResponse<MessageDto>> {
        val request = SendMessageRequest(
            content = content,
            type = "text",
            replyTo = replyTo,
            metadata = "{}"
        )
        return sendMessage(chatId, request)
    }

    // ============ ПОЛЬЗОВАТЕЛИ ============

    suspend fun searchUsers(
        query: String,
        page: Int = 1,
        limit: Int = 20,
        excludeContacts: Boolean = false,
        excludeBlocked: Boolean = true
    ): Response<UsersResponse> {
        return userApi.searchUsers(query, page, limit, excludeContacts, excludeBlocked)
    }

    suspend fun getUserById(id: Int): Response<ApiResponse<UserDto>> {
        return userApi.getUserById(id)
    }

    suspend fun getOnlineUsers(): Response<OnlineUsersResponse> {
        return userApi.getOnlineUsers()
    }

    suspend fun getUserStatus(userId: Int): Response<ApiResponse<UserStatusDto>> {
        return userApi.getUserStatus(userId)
    }

    suspend fun updateProfile(request: UpdateProfileRequest): Response<ApiResponse<UserDto>> {
        return userApi.updateProfile(request)
    }

    suspend fun changePassword(request: ChangePasswordRequest): Response<ApiResponse<Unit>> {
        return userApi.changePassword(request)
    }

    suspend fun logout(refreshToken: String? = null): Boolean {
        return try {
            val token = refreshToken ?: tokenManager.getRefreshToken()
            if (token != null) {
                val request = LogoutRequest(refreshToken = token)
                authApi.logout(request)
            }
            tokenManager.clearTokens()
            true
        } catch (e: Exception) {
            tokenManager.clearTokens()
            false
        }
    }
}