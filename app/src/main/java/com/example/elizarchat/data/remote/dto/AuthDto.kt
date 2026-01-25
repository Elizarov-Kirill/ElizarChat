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
    @SerialName("email") val email: String,  // На сервере принимает email ИЛИ username
    @SerialName("password") val password: String
)

@Serializable
data class RefreshTokenRequest(
    @SerialName("refreshToken") val refreshToken: String
)

@Serializable
data class LogoutRequest(
    @SerialName("refreshToken") val refreshToken: String
)

// ============== ОТВЕТЫ ==============
@Serializable
data class TokenResponse(
    @SerialName("accessToken") val accessToken: String,
    @SerialName("refreshToken") val refreshToken: String,
    @SerialName("expiresIn") val expiresIn: Long? = null
)

@Serializable
data class AuthResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("message") val message: String? = null,
    @SerialName("data") val data: TokenResponse? = null
)

@Serializable
data class ErrorResponse(
    @SerialName("success") val success: Boolean = false,
    @SerialName("message") val message: String,
    @SerialName("error") val error: String? = null,
    @SerialName("details") val details: Map<String, String>? = null
)