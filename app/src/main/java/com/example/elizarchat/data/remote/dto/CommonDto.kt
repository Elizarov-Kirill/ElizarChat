package com.example.elizarchat.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Базовый ответ API (формат: {success, message, data, error})
 * Используется для API ответов КРОМЕ аутентификации!
 * ⚠️ Аутентификация использует другой формат: {success, user, tokens}
 */
@Serializable
data class ApiResponse<T>(
    @SerialName("success")
    val success: Boolean,

    @SerialName("message")
    val message: String? = null,

    @SerialName("data")
    val data: T? = null,

    @SerialName("error")
    val error: String? = null
)

/**
 * Ответ со списком пользователей
 */
@Serializable
data class UsersResponse(
    @SerialName("users")
    val users: List<UserDto> = emptyList()
)

@Serializable
data class OnlineUsersResponse(
    @SerialName("online")
    val online: List<UserDto> = emptyList(),

    @SerialName("count")
    val count: Int = 0
)

/**
 * Общий запрос с пагинацией
 */
@Serializable
data class PaginationRequest(
    @SerialName("page")
    val page: Int = 1,

    @SerialName("limit")
    val limit: Int = 20
)

/**
 * Ответ с пагинацией
 */
@Serializable
data class PaginatedResponse<T>(
    @SerialName("items")
    val items: List<T>,

    @SerialName("total")
    val total: Int,

    @SerialName("page")
    val page: Int,

    @SerialName("limit")
    val limit: Int,

    @SerialName("pages")
    val pages: Int
)