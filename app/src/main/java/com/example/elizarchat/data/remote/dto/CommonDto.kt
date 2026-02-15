package com.example.elizarchat.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Базовый ответ API
 * Сервер всегда возвращает: {success, ...данные, error}
 */
@Serializable
data class ApiResponse<T>(
    @SerialName("success")
    val success: Boolean,

    @SerialName("error")
    val error: String? = null,

    // Разные типы ответов от сервера
    @SerialName("message")
    val message: String? = null,

    // Для списка чатов
    @SerialName("chats")
    val chats: List<ChatDto>? = null,

    // Для одного чата
    @SerialName("chat")
    val chat: ChatDto? = null,

    // Для сообщений
    @SerialName("messages")
    val messages: List<MessageDto>? = null,

    // Для пользователей (поиск)
    @SerialName("users")
    val users: List<UserDto>? = null,

    // Для аутентификации
    @SerialName("user")
    val user: UserDto? = null,

    @SerialName("tokens")
    val tokens: TokensDto? = null,

    // Универсальное поле для других данных
    @SerialName("data")
    val data: T? = null,

    // Пагинация
    @SerialName("pagination")
    val pagination: PaginationDto? = null
)

/**
 * DTO для токенов
 */
@Serializable
data class TokensDto(
    @SerialName("accessToken")
    val accessToken: String,

    @SerialName("refreshToken")
    val refreshToken: String
)

/**
 * Ответ со списком пользователей (для поиска)
 */
@Serializable
data class UsersResponse(
    @SerialName("users")
    val users: List<UserDto> = emptyList(),

    @SerialName("pagination")
    val pagination: PaginationDto? = null
)

@Serializable
data class MessagesResponse(
    @SerialName("messages")
    val messages: List<MessageDto> = emptyList(),

    @SerialName("pagination")
    val pagination: PaginationDto? = null,

    @SerialName("hasMore")
    val hasMore: Boolean = false,

    @SerialName("total")
    val total: Int = 0
)

/**
 * Пагинация
 */
@Serializable
data class PaginationDto(
    @SerialName("query")
    val query: String? = null,

    @SerialName("limit")
    val limit: Int = 20,

    @SerialName("offset")
    val offset: Int = 0,

    @SerialName("hasMore")
    val hasMore: Boolean = false,

    @SerialName("total")
    val total: Int? = null
)

/**
 * Ответ с онлайн пользователями
 */
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
 * Ответ с пагинацией (универсальный)
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

// Добавьте в конец CommonDto.kt:

/**
 * Статус пользователя
 */
@Serializable
data class UserStatusDto(
    @SerialName("userId")
    val userId: Int,

    @SerialName("isOnline")
    val isOnline: Boolean,

    @SerialName("lastSeen")
    val lastSeen: String? = null,

    @SerialName("status")
    val status: String? = null
)

/**
 * Запрос на обновление профиля
 */
@Serializable
data class UpdateProfileRequest(
    @SerialName("displayName")
    val displayName: String? = null,

    @SerialName("avatarUrl")
    val avatarUrl: String? = null,

    @SerialName("bio")
    val bio: String? = null
)

/**
 * Запрос на смену пароля
 */
@Serializable
data class ChangePasswordRequest(
    @SerialName("currentPassword")
    val currentPassword: String,

    @SerialName("newPassword")
    val newPassword: String
)