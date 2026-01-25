package com.example.elizarchat.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ============ CHAT DTOs ============

/**
 * DTO чата - точное соответствие серверному API
 * Согласно таблице: chats(id, type, name, description, created_by,
 * created_at, updated_at, last_message_id)
 */
@Serializable
data class ChatDto(
    @SerialName("id")
    val id: String,

    @SerialName("type")
    val type: String,  // "private", "group", "channel"

    @SerialName("name")
    val name: String? = null,

    @SerialName("description")
    val description: String? = null,

    @SerialName("created_by")
    val createdBy: String,

    @SerialName("created_at")
    val createdAt: String? = null,  // ISO 8601 строка

    @SerialName("updated_at")
    val updatedAt: String? = null,  // ISO 8601 строка

    @SerialName("last_message_id")
    val lastMessageId: String? = null,

    // Опционально: участники чата
    @SerialName("members")
    val members: List<ChatMemberDto>? = null
)

/**
 * DTO участника чата
 */
@Serializable
data class ChatMemberDto(
    @SerialName("id")
    val id: Long,

    @SerialName("chat_id")
    val chatId: String,

    @SerialName("user_id")
    val userId: String,

    @SerialName("role")
    val role: String,  // "owner", "admin", "member", "guest"

    @SerialName("joined_at")
    val joinedAt: String? = null,  // ISO 8601 строка

    @SerialName("last_read_message_id")
    val lastReadMessageId: String? = null,

    @SerialName("created_at")
    val createdAt: String? = null,  // ISO 8601 строка

    @SerialName("updated_at")
    val updatedAt: String? = null  // ISO 8601 строка
)

// ============ CHAT REQUESTS ============

/**
 * Создание чата
 * POST /api/v1/chats
 */
@Serializable
data class CreateChatRequest(
    @SerialName("type")
    val type: String,  // "private", "group", "channel"

    @SerialName("name")
    val name: String? = null,

    @SerialName("description")
    val description: String? = null,

    @SerialName("member_ids")
    val memberIds: List<String>  // ID пользователей для добавления
)

/**
 * Обновление чата
 * PUT /api/v1/chats/:id
 */
@Serializable
data class UpdateChatRequest(
    @SerialName("name")
    val name: String? = null,

    @SerialName("description")
    val description: String? = null
)

/**
 * Добавление участника
 * POST /api/v1/chats/:id/members
 */
@Serializable
data class AddMemberRequest(
    @SerialName("user_id")
    val userId: String,

    @SerialName("role")
    val role: String = "member"  // "owner", "admin", "member"
)

/**
 * Удаление участника
 * DELETE /api/v1/chats/:id/members
 */
@Serializable
data class RemoveMemberRequest(
    @SerialName("user_id")
    val userId: String
)

// ============ CHAT RESPONSES ============

/**
 * Ответ со списком чатов
 */
@Serializable
data class ChatsResponse(
    @SerialName("chats")
    val chats: List<ChatDto> = emptyList()
)