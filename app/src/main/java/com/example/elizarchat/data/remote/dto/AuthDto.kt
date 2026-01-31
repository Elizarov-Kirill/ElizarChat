package com.example.elizarchat.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ============== ЗАПРОСЫ ==============
@Serializable
data class RegisterRequest(
    @SerialName("username") val username: String,
    @SerialName("email") val email: String,
    @SerialName("password") val password: String,
    @SerialName("display_name") val displayName: String? = null
)

@Serializable
data class LoginRequest(
    @SerialName("email") val email: String,  // Принимает email ИЛИ username в этом поле
    @SerialName("password") val password: String
)

@Serializable
data class RefreshTokenRequest(
    @SerialName("refresh_token") val refreshToken: String  // snake_case для сервера
)

@Serializable
data class LogoutRequest(
    @SerialName("refresh_token") val refreshToken: String  // snake_case для сервера
)

// ============== ОТВЕТЫ ==============
@Serializable
data class TokenResponse(
    @SerialName("access_token") val accessToken: String,  // snake_case для сервера
    @SerialName("refresh_token") val refreshToken: String,  // snake_case для сервера
    @SerialName("expires_in") val expiresIn: Long? = null  // snake_case для сервера
)

@Serializable
data class AuthResponse(
    @SerialName("access_token") val accessToken: String,  // snake_case для сервера
    @SerialName("refresh_token") val refreshToken: String,  // snake_case для сервера
    @SerialName("user") val user: UserDto  // Полная информация о пользователе
)