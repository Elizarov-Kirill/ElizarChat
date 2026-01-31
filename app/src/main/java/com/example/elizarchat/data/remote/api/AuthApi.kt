package com.example.elizarchat.data.remote.api

import com.example.elizarchat.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface AuthApi {

    // === РЕГИСТРАЦИЯ И АУТЕНТИФИКАЦИЯ ===

    /**
     * POST /api/v1/auth/register
     * Регистрация нового пользователя
     */
    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<ApiResponse<AuthResponse>>  // Изменено: TokenResponse → AuthResponse

    /**
     * POST /api/v1/auth/login
     * Вход пользователя
     * Принимает email ИЛИ username в поле email
     */
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<ApiResponse<AuthResponse>>  // Изменено: TokenResponse → AuthResponse

    /**
     * GET /api/v1/auth/me
     * Получение информации о текущем пользователе по токену
     */
    @GET("auth/me")
    suspend fun getCurrentUser(): Response<ApiResponse<UserDto>>  // Добавлено

    /**
     * POST /api/v1/auth/validate
     * Проверка валидности access токена
     */
    @POST("auth/validate")
    suspend fun validateToken(): Response<ApiResponse<ValidateTokenResponse>>  // Добавлено

    // === ТОКЕНЫ ===

    /**
     * POST /api/v1/auth/refresh
     * Обновление access токена
     */
    @POST("auth/refresh")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): Response<ApiResponse<AuthResponse>>  // Изменено: TokenResponse → AuthResponse

    /**
     * POST /api/v1/auth/logout
     * Выход пользователя (инвалидация refresh токена)
     */
    @POST("auth/logout")
    suspend fun logout(
        @Body request: LogoutRequest
    ): Response<ApiResponse<Unit>>

    /**
     * POST /api/v1/auth/logout/all
     * Выход со всех устройств
     */
    @POST("auth/logout/all")
    suspend fun logoutAll(): Response<ApiResponse<Unit>>  // Добавлено

    // === ВОССТАНОВЛЕНИЕ ПАРОЛЯ ===

    /**
     * POST /api/v1/auth/forgot-password
     * Запрос на восстановление пароля
     */
    @POST("auth/forgot-password")
    suspend fun forgotPassword(
        @Body request: ForgotPasswordRequest
    ): Response<ApiResponse<Unit>>  // Добавлено

    /**
     * POST /api/v1/auth/reset-password
     * Сброс пароля по токену
     */
    @POST("auth/reset-password")
    suspend fun resetPassword(
        @Body request: ResetPasswordRequest
    ): Response<ApiResponse<Unit>>  // Добавлено

    /**
     * POST /api/v1/auth/verify-reset-token
     * Проверка токена сброса пароля
     */
    @POST("auth/verify-reset-token")
    suspend fun verifyResetToken(
        @Body request: VerifyResetTokenRequest
    ): Response<ApiResponse<Unit>>  // Добавлено

    // === ВЕРИФИКАЦИЯ АККАУНТА ===

    /**
     * POST /api/v1/auth/verify-email
     * Отправка email для верификации
     */
    @POST("auth/verify-email")
    suspend fun sendVerificationEmail(
        @Body request: SendVerificationEmailRequest
    ): Response<ApiResponse<Unit>>  // Добавлено

    /**
     * POST /api/v1/auth/verify-email/confirm
     * Подтверждение email
     */
    @POST("auth/verify-email/confirm")
    suspend fun verifyEmail(
        @Body request: VerifyEmailRequest
    ): Response<ApiResponse<Unit>>  // Добавлено

    // === СИСТЕМНЫЕ ===

    /**
     * GET /api/v1/health
     * Проверка работоспособности API
     */
    @GET("health")
    suspend fun healthCheck(): Response<ApiResponse<HealthResponse>>
}

// === DTO ДЛЯ ДОПОЛНИТЕЛЬНЫХ ЗАПРОСОВ ===

@kotlinx.serialization.Serializable
data class ValidateTokenResponse(
    @kotlinx.serialization.SerialName("valid")
    val valid: Boolean,
    @kotlinx.serialization.SerialName("user_id")
    val userId: Int? = null,
    @kotlinx.serialization.SerialName("expires_in")
    val expiresIn: Long? = null
)

@kotlinx.serialization.Serializable
data class ForgotPasswordRequest(
    @kotlinx.serialization.SerialName("email")
    val email: String  // email или username
)

@kotlinx.serialization.Serializable
data class ResetPasswordRequest(
    @kotlinx.serialization.SerialName("token")
    val token: String,
    @kotlinx.serialization.SerialName("new_password")
    val newPassword: String
)

@kotlinx.serialization.Serializable
data class VerifyResetTokenRequest(
    @kotlinx.serialization.SerialName("token")
    val token: String
)

@kotlinx.serialization.Serializable
data class SendVerificationEmailRequest(
    @kotlinx.serialization.SerialName("email")
    val email: String
)

@kotlinx.serialization.Serializable
data class VerifyEmailRequest(
    @kotlinx.serialization.SerialName("token")
    val token: String
)