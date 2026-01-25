package com.example.elizarchat.data.remote.api

import com.example.elizarchat.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {

    /**
     * POST /api/v1/auth/register
     * Регистрация нового пользователя
     */
    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<ApiResponse<TokenResponse>>

    /**
     * POST /api/v1/auth/login
     * Вход пользователя
     */
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<ApiResponse<TokenResponse>>

    /**
     * POST /api/v1/auth/refresh
     * Обновление access токена
     */
    @POST("auth/refresh")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): Response<ApiResponse<TokenResponse>>

    /**
     * POST /api/v1/auth/logout
     * Выход пользователя (инвалидация refresh токена)
     */
    @POST("auth/logout")
    suspend fun logout(
        @Body request: RefreshTokenRequest  // Используем тот же DTO что и для refresh
    ): Response<ApiResponse<Unit>>

    /**
     * GET /api/v1/health
     * Проверка работоспособности API
     */
    @GET("health")
    suspend fun healthCheck(): Response<ApiResponse<String>>
}