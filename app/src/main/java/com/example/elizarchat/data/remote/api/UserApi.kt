package com.example.elizarchat.data.remote.api

import com.example.elizarchat.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface UserApi {

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
     * GET /api/v1/users/search?query=...
     * Поиск пользователей
     */
    @GET("users/search")
    suspend fun searchUsers(
        @Query("query") query: String
    ): Response<ApiResponse<List<UserDto>>>

    /**
     * GET /api/v1/users/online
     * Список онлайн пользователей
     */
    @GET("users/online")
    suspend fun getOnlineUsers(): Response<ApiResponse<List<UserDto>>>

    /**
     * GET /api/v1/users/{id}
     * Профиль пользователя по ID
     */
    @GET("users/{id}")
    suspend fun getUserById(
        @Path("id") id: String
    ): Response<ApiResponse<UserDto>>
}