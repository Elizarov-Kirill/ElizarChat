package com.example.elizarchat.data.remote.api

import com.example.elizarchat.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface UserApi {

    // === ТЕКУЩИЙ ПОЛЬЗОВАТЕЛЬ ===

    /**
     * GET /api/v1/users/me
     * Получение текущего профиля
     */
    @GET("users/me")
    suspend fun getCurrentUser(): Response<ApiResponse<UserDto>>

    /**
     * PUT /api/v1/users/me
     * Обновление профиля
     */
    @PUT("users/me")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest
    ): Response<ApiResponse<UserDto>>

    /**
     * POST /api/v1/users/me/change-password
     * Смена пароля
     */
    @POST("users/me/change-password")
    suspend fun changePassword(
        @Body request: ChangePasswordRequest
    ): Response<ApiResponse<Unit>>

    /**
     * PUT /api/v1/users/me/settings
     * Обновление настроек
     */
    @PUT("users/me/settings")
    suspend fun updateSettings(
        @Body request: UpdateSettingsRequest
    ): Response<ApiResponse<Unit>>

    /**
     * PUT /api/v1/users/me/status
     * Обновление статуса (online/offline/busy/away)
     */
    @PUT("users/me/status")
    suspend fun updateStatus(
        @Body request: UpdateUserStatusRequest
    ): Response<ApiResponse<UserDto>>  // Добавлено

    /**
     * POST /api/v1/users/me/avatar
     * Загрузка аватара
     */
    @Multipart
    @POST("users/me/avatar")
    suspend fun uploadAvatar(
        @Part file: okhttp3.MultipartBody.Part
    ): Response<ApiResponse<UserDto>>  // Добавлено

    // === ПОЛЬЗОВАТЕЛИ ===

    /**
     * GET /api/v1/users/{id}
     * Профиль пользователя по ID
     */
    @GET("users/{user_id}")
    suspend fun getUserById(
        @Path("user_id") id: Int  // Изменено: String → Int, snake_case
    ): Response<ApiResponse<UserDto>>

    /**
     * GET /api/v1/users/batch
     * Получение нескольких пользователей по ID
     */
    @GET("users/batch")
    suspend fun getUsersByIds(
        @Query("ids") ids: List<Int>  // Добавлено
    ): Response<ApiResponse<List<UserDto>>>

    /**
     * GET /api/v1/users
     * Получение списка пользователей
     */
    @GET("users")
    suspend fun getUsers(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("online_only") onlineOnly: Boolean = false,
        @Query("recently_active") recentlyActive: Boolean = false
    ): Response<ApiResponse<UsersResponse>>

    // === ПОИСК ===

    /**
     * GET /api/v1/users/search
     * Поиск пользователей
     */
    @GET("users/search")
    suspend fun searchUsers(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("exclude_contacts") excludeContacts: Boolean = false,
        @Query("exclude_blocked") excludeBlocked: Boolean = true
    ): Response<UsersResponse>

    // === ОНЛАЙН СТАТУСЫ ===

    /**
     * GET /api/v1/users/online
     * Список онлайн пользователей
     */
    @GET("users/online")
    suspend fun getOnlineUsers(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Response<ApiResponse<OnlineUsersResponse>>

    /**
     * GET /api/v1/users/status/{id}
     * Получение статуса пользователя
     */
    @GET("users/status/{user_id}")
    suspend fun getUserStatus(
        @Path("user_id") id: Int  // Добавлено
    ): Response<ApiResponse<UserStatusResponse>>

    /**
     * GET /api/v1/users/status/batch
     * Получение статусов нескольких пользователей
     */
    @GET("users/status/batch")
    suspend fun getUsersStatus(
        @Query("ids") ids: List<Int>  // Добавлено
    ): Response<ApiResponse<Map<Int, UserStatusResponse>>>

    // === КОНТАКТЫ ===

    /**
     * GET /api/v1/users/contacts
     * Получение списка контактов
     */
    @GET("users/contacts")
    suspend fun getContacts(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Response<ApiResponse<UsersResponse>>  // Добавлено

    /**
     * POST /api/v1/users/contacts/{id}
     * Добавление пользователя в контакты
     */
    @POST("users/contacts/{user_id}")
    suspend fun addContact(
        @Path("user_id") id: Int  // Добавлено
    ): Response<ApiResponse<Unit>>

    /**
     * DELETE /api/v1/users/contacts/{id}
     * Удаление пользователя из контактов
     */
    @DELETE("users/contacts/{user_id}")
    suspend fun removeContact(
        @Path("user_id") id: Int  // Добавлено
    ): Response<ApiResponse<Unit>>

    /**
     * GET /api/v1/users/blocked
     * Получение списка заблокированных пользователей
     */
    @GET("users/blocked")
    suspend fun getBlockedUsers(): Response<ApiResponse<UsersResponse>>  // Добавлено

    /**
     * POST /api/v1/users/block/{id}
     * Блокировка пользователя
     */
    @POST("users/block/{user_id}")
    suspend fun blockUser(
        @Path("user_id") id: Int  // Добавлено
    ): Response<ApiResponse<Unit>>

    /**
     * DELETE /api/v1/users/block/{id}
     * Разблокировка пользователя
     */
    @DELETE("users/block/{user_id}")
    suspend fun unblockUser(
        @Path("user_id") id: Int  // Добавлено
    ): Response<ApiResponse<Unit>>

    // === ВАЛИДАЦИЯ ===

    /**
     * GET /api/v1/users/check-username
     * Проверка доступности username
     */
    @GET("users/check-username")
    suspend fun checkUsernameAvailability(
        @Query("username") username: String
    ): Response<ApiResponse<AvailabilityResponse>>  // Добавлено

    /**
     * GET /api/v1/users/check-email
     * Проверка доступности email
     */
    @GET("users/check-email")
    suspend fun checkEmailAvailability(
        @Query("email") email: String
    ): Response<ApiResponse<AvailabilityResponse>>  // Добавлено
}

/**
 * Запрос для обновления статуса пользователя
 */
@kotlinx.serialization.Serializable
data class UpdateUserStatusRequest(
    @kotlinx.serialization.SerialName("status")
    val status: String?,  // "online", "offline", "busy", "away", null для авто
    @kotlinx.serialization.SerialName("custom_status")
    val customStatus: String? = null
)

/**
 * Ответ со статусом пользователя
 */
@kotlinx.serialization.Serializable
data class UserStatusResponse(
    @kotlinx.serialization.SerialName("user_id")
    val userId: Int,
    @kotlinx.serialization.SerialName("is_online")
    val isOnline: Boolean,
    @kotlinx.serialization.SerialName("last_seen")
    val lastSeen: String?,
    @kotlinx.serialization.SerialName("status")
    val status: String?
)

/**
 * Ответ проверки доступности
 */
@kotlinx.serialization.Serializable
data class AvailabilityResponse(
    @kotlinx.serialization.SerialName("available")
    val available: Boolean,
    @kotlinx.serialization.SerialName("suggestions")
    val suggestions: List<String> = emptyList()
)