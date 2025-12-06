package com.example.elizarchat.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    @SerialName("id")
    val id: Long,

    @SerialName("username")
    val username: String,

    @SerialName("email")
    val email: String? = null,

    @SerialName("displayName")
    val displayName: String? = null,

    @SerialName("avatarUrl")
    val avatarUrl: String? = null,

    @SerialName("isOnline")
    val isOnline: Boolean = false,

    @SerialName("lastSeen")
    val lastSeen: String? = null,

    @SerialName("createdAt")
    val createdAt: String? = null
)

/**
 * Ответ на запрос аутентификации
 */
@Serializable
data class AuthResponseDto(
    @SerialName("token")
    val token: String,

    @SerialName("user")
    val user: UserDto
)

/**
 * Ответ на поиск пользователей
 */
@Serializable
data class UsersResponseDto(
    @SerialName("users")
    val users: List<UserDto> = emptyList()
)