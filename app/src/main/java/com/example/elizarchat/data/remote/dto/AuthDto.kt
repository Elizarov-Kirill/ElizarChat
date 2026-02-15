package com.example.elizarchat.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ============== ЗАПРОСЫ ==============
@Serializable
data class RegisterRequest(
    @SerialName("username")
    val username: String,

    @SerialName("email")
    val email: String,

    @SerialName("password")
    val password: String,

    @SerialName("displayName")
    val displayName: String? = null
)

@Serializable
data class LoginRequest(
    @SerialName("email")
    val email: String,

    @SerialName("password")
    val password: String
)

@Serializable
data class RefreshTokenRequest(
    @SerialName("refreshToken")
    val refreshToken: String
)

@Serializable
data class LogoutRequest(
    @SerialName("refreshToken")
    val refreshToken: String
)

// ============== ОТВЕТЫ ==============

// ДЛЯ АУТЕНТИФИКАЦИИ (login, register, refresh)
@Serializable
data class AuthResponse(
    @SerialName("success")
    val success: Boolean,

    @SerialName("user")
    val user: UserDto,

    @SerialName("tokens")
    val tokens: TokensDto,  // Используйте TokensDto, а не TokensResponse!

    @SerialName("error")
    val error: String? = null
)
