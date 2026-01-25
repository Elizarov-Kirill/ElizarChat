package com.example.elizarchat.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Базовый ответ API (формат: {success, message, data})
 */
@Serializable
data class ApiResponse<T>(
    @SerialName("success") val success: Boolean,
    @SerialName("message") val message: String? = null,
    @SerialName("data") val data: T? = null
)

/**
 * Ответ со списком пользователей
 */
@Serializable
data class UsersResponse(
    @SerialName("users") val users: List<UserDto> = emptyList()
)

@Serializable
data class OnlineUsersResponse(
    @SerialName("online") val online: List<UserDto> = emptyList(),
    @SerialName("count") val count: Int = 0
)